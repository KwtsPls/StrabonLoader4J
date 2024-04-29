package gr.uoa.di.ai.handlers;

import gr.uoa.di.ai.Loader;
import gr.uoa.di.ai.constants.AllValueTypes;
import gr.uoa.di.ai.constants.LoaderGeneralConstants;
import gr.uoa.di.ai.constants.LoaderGlobals;
import gr.uoa.di.ai.types.LabelDatatype;
import gr.uoa.di.ai.types.LabelLang;
import gr.uoa.di.ai.types.PredicateContent;
import gr.uoa.di.ai.types._Triple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.*;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.geotools.referencing.CRS;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TripleHandler {

    public TripleHandler(){

    }

    public void handle(Triple triple) {
        int subjId, predId, objId;
        long hashValueSubj,hashValuePred,hashValueObj;

        boolean mustStore = true;

        Node subject = triple.getSubject();

        // CASE FOR BLANK NODE SUBJECT
        if (subject.isBlank()) {
            String bnodeAlias;
            Node_Blank blankNode = (Node_Blank) subject;

            if (!LoaderGlobals.bnodeAssignments.containsKey(blankNode.toString())) {
                // New blank node
                bnodeAlias = createBNode();
                LoaderGlobals.bnodeAssignments.put(blankNode.toString(), bnodeAlias);

                subjId = ValueHandler.assignId(AllValueTypes.BNODE);
                LoaderGlobals.bnode_values.put(bnodeAlias, subjId);
            } else {
                // Existing blank node
                String bnodeKey = LoaderGlobals.bnodeAssignments.get(blankNode.toString());
                subjId = LoaderGlobals.bnode_values.get(bnodeKey);
                mustStore = false;
            }
        }
        // CASE FOR URI SUBJECT
        else{
            if(LoaderGlobals.previousURI.equals(subject.toString())){
                subjId = LoaderGlobals.previousId;
                mustStore = false;
            }
            else{
                Integer uri_id = LoaderGlobals.uri_values.get(subject.toString());
                Integer long_uri_id = LoaderGlobals.long_uri_values.get(subject.toString());

                if(uri_id==null && long_uri_id==null){
                    if(ValueHandler.isLong(subject.toString())){
                        subjId = ValueHandler.assignId(AllValueTypes.URI_LONG);
                        LoaderGlobals.long_uri_values.put(subject.toString(),subjId);
                    }
                    else{
                        subjId = ValueHandler.assignId(AllValueTypes.URI);
                        LoaderGlobals.uri_values.put(subject.toString(),subjId);
                    }
                }
                else{
                    if(uri_id!=null)
                        subjId = uri_id;
                    else
                        subjId = long_uri_id;

                    mustStore = false;
                }

                LoaderGlobals.previousURI = subject.toString();
                LoaderGlobals.previousId = subjId;
            }
        }

        if(mustStore){
            hashValueSubj = hashOf(subject.toString(),getNodeType(subject),null,null);
            try {
                LoaderGlobals.hashOutput.write(subjId + "," + hashValueSubj + "\n");
            } catch (IOException e) {
                System.out.println("File append for"+LoaderGeneralConstants.CSV_HASH+"failed!!");
                e.printStackTrace();
                System.exit(10);
            }
        }
        else{
            mustStore=true;
        }

        //CASE FOR PREDICATE - PREDICATE CAN ONLY BE A URI
        BufferedWriter whereToStore = null;
        Node predicate = triple.getPredicate();

        PredicateContent pc = LoaderGlobals.predicates.get(predicate.toString());
        if(pc==null){
            Integer uri_id = LoaderGlobals.uri_values.get(subject.toString());
            Integer long_uri_id = LoaderGlobals.long_uri_values.get(subject.toString());

            if(uri_id==null && long_uri_id==null){
                if(ValueHandler.isLong(subject.toString())){
                    predId = ValueHandler.assignId(AllValueTypes.URI_LONG);
                    LoaderGlobals.long_uri_values.put(subject.toString(),subjId);
                }
                else{
                    predId = ValueHandler.assignId(AllValueTypes.URI);
                    LoaderGlobals.uri_values.put(subject.toString(),subjId);
                }
            }
            else{
                if(uri_id!=null)
                    predId = uri_id;
                else
                    predId = long_uri_id;

                mustStore = false;
            }

            PredicateContent content;
            StringBuilder predTableName = new StringBuilder();
            predTableName.append(createName(predicate.toString())).append("_").append(predId).append(".csv");
            BufferedWriter newPredFile=null;
            try {
                newPredFile = new BufferedWriter(new FileWriter(predTableName.toString()));
            } catch (IOException e) {
                System.out.println("File creation for predicate failed!! " + predTableName.toString());
                e.printStackTrace();
                System.exit(10);
            }

            LoaderGlobals.streamPool.add(newPredFile);
            whereToStore = LoaderGlobals.streamPool.get(LoaderGlobals.streamPool.size()-1);
            content = new PredicateContent(predId,newPredFile);

            LoaderGlobals.predicates.put(predicate.toString(),content);
        }
        //Must simply find the stream to write to and assign the id to store
        else{
            PredicateContent tmp = LoaderGlobals.predicates.get(predicate.toString());
            predId = tmp.getId();
            whereToStore = tmp.getPredTable();

            //No reason to store in hash_values
            mustStore = false;
        }

        if(mustStore){
            hashValuePred = hashOf(predicate.toString(),getNodeType(predicate),null,null);
            try {
                LoaderGlobals.hashOutput.write(predId + "," + hashValuePred + "\n");
            } catch (IOException e) {
                System.out.println("File append for"+LoaderGeneralConstants.CSV_HASH+"failed!!");
                e.printStackTrace();
                System.exit(10);
            }
        }
        else{
            mustStore=true;
        }


        //OBJECT HANDLING
        Node object = triple.getObject();

        if(getNodeType(object).equals("LITERAL")){
            String parsedObject = escapeCommas(object.toString());

            //Case 1: No Specific Datatype => LANG or SIMPLE
            if(getNodeDatatype(object)==null){
                LabelLang l1 = null;

                Integer long_label_values_id = null;
                Integer label_values_id = null;
                Integer long_label_lang_id = null;
                Integer label_lang_id = null;

                boolean condition = false; //I want it to be true in order to store anything


                boolean lang=false;
                if(getNodeLanguage(object)!=null){
                    lang = true;
                    l1 = new LabelLang(parsedObject,getNodeLanguage(object));

                    long_label_lang_id = LoaderGlobals.long_label_lang.get(l1);
                    label_lang_id = LoaderGlobals.label_lang.get(l1);
                    condition = long_label_lang_id!=null || label_lang_id!=null;
                }
                else{
                    long_label_values_id = LoaderGlobals.long_label_values.get(parsedObject);
                    label_values_id = LoaderGlobals.label_values.get(parsedObject);
                    condition = long_label_values_id!=null || label_values_id!=null;
                }


                if(condition){
                    //Insert in label_values or long_label_values, depending on the value's length
                    if(ValueHandler.isLong(object.toString())){

                        if(lang){
                            objId = ValueHandler.assignId(AllValueTypes.LANG_LONG);
                            LoaderGlobals.long_label_lang.put(l1,objId);
                        }
                        else{
                            objId = ValueHandler.assignId(AllValueTypes.SIMPLE_LONG);
                            LoaderGlobals.long_label_values.put(parsedObject,objId);
                        }

                    }
                    else{

                        if(lang){
                            objId = ValueHandler.assignId(AllValueTypes.LANG);
                            LoaderGlobals.label_lang.put(l1,objId);
                        }
                        else{
                            objId = ValueHandler.assignId(AllValueTypes.SIMPLE);
                            LoaderGlobals.label_values.put(parsedObject,objId);
                        }

                    }

                    if(lang){
                        try {
                            LoaderGlobals.languageOutput.write(objId+","+getNodeLanguage(object)+"\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                else{

                    if(!lang){
                        if(long_label_values_id!=null){
                            objId = long_label_values_id;
                        }
                        else{
                            objId = label_values_id;
                        }
                    }
                    else{
                        if(long_label_lang_id!=null){
                            objId = long_label_lang_id;
                        }
                        else{
                            objId = label_lang_id;
                        }
                    }
                    mustStore=false;
                }

            }
            //TYPED LITERAL CASES
            else{

                LabelDatatype l2 = new LabelDatatype(parsedObject,getNodeDatatype(object));
                Integer long_label_datatype_id = LoaderGlobals.long_label_datatype.get(l2);
                Integer label_datatype_id = LoaderGlobals.label_datatype.get(l2);

                if(long_label_datatype_id==null && label_datatype_id==null){

                    Integer long_uri_values_id = LoaderGlobals.long_uri_values.get(getNodeDatatype(object));
                    Integer uri_values_id = LoaderGlobals.uri_values.get(getNodeDatatype(object));

                    if(long_uri_values_id==null && uri_values_id==null){

                        int datatypeId;

                        if(ValueHandler.isLong(getNodeDatatype(object))){
                            datatypeId = ValueHandler.assignId(AllValueTypes.URI_LONG);
                            LoaderGlobals.long_uri_values.put(getNodeDatatype(object),datatypeId);
                        }
                        else{
                            datatypeId = ValueHandler.assignId(AllValueTypes.URI);
                            LoaderGlobals.uri_values.put(getNodeDatatype(object),datatypeId);
                        }

                        try {
                            LoaderGlobals.hashOutput.write(datatypeId + "," + hashMine(getNodeDatatype(object)) + "\n");
                        } catch (IOException e) {
                            System.out.println("File append for"+LoaderGeneralConstants.CSV_HASH+"failed!!");
                            e.printStackTrace();
                            System.exit(10);
                        }
                    }

                    //3 cases: Numeric, DateTime and SimpleLiteral
                    if(ValueHandler.isNumeric(getNodeDatatype(object))){
                        objId = ValueHandler.assignId(AllValueTypes.NUMERIC);

                        try {
                            LoaderGlobals.numericsOutput.write(objId+","+Double.parseDouble(object.toString())+"\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        LoaderGlobals.label_datatype.put(l2,objId);

                        try {
                            LoaderGlobals.datatypeOutput.write(objId+","+getNodeDatatype(object)+"\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(ValueHandler.isCalendar(getNodeDatatype(object))){

                        if(ValueHandler.isZoned(object.toString())){
                            objId = ValueHandler.assignId(AllValueTypes.DATETIME_ZONED);
                        }
                        else{
                            objId = ValueHandler.assignId(AllValueTypes.DATETIME);
                        }
                        LoaderGlobals.label_datatype.put(l2,objId);

                        try {
                            LoaderGlobals.datatypeOutput.write(objId+","+getNodeDatatype(object)+"\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            LoaderGlobals.datetimeOutput.write(objId+","+convertDateTimeToMilliseconds(object.toString())+"\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //Simplest and most general case
                    else{

                        if(ValueHandler.isLong(object.toString()) || ValueHandler.isXML(object.toString())){

                            if(ValueHandler.isXML(getNodeDatatype(object))){
                                objId = ValueHandler.assignId(AllValueTypes.XML);
                            }
                            else{
                                objId = ValueHandler.assignId(AllValueTypes.TYPED_LONG);
                            }
                            LoaderGlobals.long_label_datatype.put(l2,objId);

                        }
                        else{
                            objId = ValueHandler.assignId(AllValueTypes.TYPED);
                            LoaderGlobals.label_datatype.put(l2,objId);
                        }

                        if(ValueHandler.isSpatial(getNodeDatatype(object))){

                            String spatialLiteral = object.toString();
                            int srid = 4326;
                            Geometry geosGeom=null;

                            String wkt;
                            int locateDelimiter = spatialLiteral.indexOf(';');

                            // Default value
                            if (locateDelimiter == -1) {
                                // No SRID specified => 4326
                                wkt = spatialLiteral;
                            } else {
                                wkt = spatialLiteral.substring(0, locateDelimiter);
                                // Extract SRID from the spatial literal
                                String[] value = object.toString().split("\\^\\^");

                                String sridStr = value[0].substring(value[0].lastIndexOf('/') + 1);
                                srid = Integer.parseInt(sridStr.replaceAll("\"",""));
                            }

                            try {
                                LoaderGlobals.geoOutput.write((objId + ",").getBytes(),(objId + ",").getBytes().length);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (srid != 4326) {
                                // Apply coordinate transformation
                                CoordinateReferenceSystem sourceCRS = null;
                                try {
                                    sourceCRS = CRS.decode("EPSG:" + srid);
                                    CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
                                    MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

                                    // Convert WKT to GEOS geometry directly
                                    WKTReader wktReader = new WKTReader();
                                    Geometry geomTransformed = wktReader.read(spatialLiteral.replace("\"",""));
                                    Geometry transformedGeometry = JTS.transform(geomTransformed,transform);

                                    // Convert JTS geometry to WKB
                                    WKBWriter wkbWriter = new WKBWriter();
                                    byte[] binaryOutput = wkbWriter.write(transformedGeometry);

                                    // Convert WKB to GEOS geometry
                                    WKBReader wkbReader = new WKBReader();
                                    geosGeom = wkbReader.read(new InputStreamInStream(new ByteArrayInputStream(binaryOutput)));

                                } catch (FactoryException | TransformException | ParseException | IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Convert WKT to GEOS geometry directly
                                WKTReader wktReader = new WKTReader();
                                try {
                                    geosGeom = wktReader.read(wkt.replace("\"",""));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            // Write GEOS geometry to output stream in HEX format
                            WKBWriter wkbWriter = new WKBWriter();
                            try {
                                wkbWriter.write(geosGeom, LoaderGlobals.geoOutput);
                                String geoHex = WKBWriter.toHex(geosGeom.toString().getBytes());
                                LoaderGlobals.geoOutput.write(geoHex.getBytes(),geoHex.getBytes().length);
                                LoaderGlobals.geoOutput.write(("," + srid + "\n").getBytes(),("," + srid + "\n").getBytes().length);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            LoaderGlobals.datatypeOutput.write(objId+","+getNodeDatatype(object)+"\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }
                else{
                    if(long_label_datatype_id!=null)
                        objId = long_label_datatype_id;
                    else
                        objId = label_datatype_id;
                    mustStore=false;
                }

            }
        }
        //BNODE CASE
        else if(getNodeType(object).equals("BNODE")){
            String bnodeAlias;
            Node_Blank blankNode = (Node_Blank) object;

            if (!LoaderGlobals.bnodeAssignments.containsKey(blankNode.toString())) {
                // New blank node
                bnodeAlias = createBNode();
                LoaderGlobals.bnodeAssignments.put(blankNode.toString(), bnodeAlias);

                objId = ValueHandler.assignId(AllValueTypes.BNODE);
                LoaderGlobals.bnode_values.put(bnodeAlias, objId);
            } else {
                // Existing blank node
                String bnodeKey = LoaderGlobals.bnodeAssignments.get(blankNode.toString());
                objId = LoaderGlobals.bnode_values.get(bnodeKey);
                mustStore = false;
            }
        }
        //URI --> Store namespace elsewhere
        else{
            Integer uri_id = LoaderGlobals.uri_values.get(object.toString());
            Integer long_uri_id = LoaderGlobals.long_uri_values.get(object.toString());

            if(uri_id==null && long_uri_id==null){
                if(ValueHandler.isLong(object.toString())){
                    objId = ValueHandler.assignId(AllValueTypes.URI_LONG);
                    LoaderGlobals.long_uri_values.put(object.toString(),objId);
                }
                else{
                    objId = ValueHandler.assignId(AllValueTypes.URI);
                    LoaderGlobals.uri_values.put(object.toString(),objId);
                }
            }
            else{
                if(uri_id!=null)
                    objId = uri_id;
                else
                    objId = long_uri_id;

                mustStore = false;
            }
        }

        if(mustStore){
            hashValueObj = hashOf(object.toString(),getNodeType(object),getNodeDatatype(object),getNodeLanguage(object));
            try {
                LoaderGlobals.hashOutput.write(objId + "," + hashValueObj + "\n");
            } catch (IOException e) {
                System.out.println("File append for"+LoaderGeneralConstants.CSV_HASH+"failed!!");
                e.printStackTrace();
                System.exit(10);
            }
        }

        _Triple tmp = new _Triple(LoaderGlobals.graphId,subjId,predId,objId,true);

        try {
            whereToStore.write(tmp.getCtx()+","+tmp.getSubj()+","+tmp.getPred()+","+tmp.getObj()+","+tmp.isExpl()+"\n");
        } catch (IOException e) {
            System.out.println("File append for Predicate Table failed!!");
            e.printStackTrace();
            System.exit(10);
        }

    }


    //HELPER METHODS

    public static Long hashOf(String value, String valueType, String datatype, String language){
        long span = 1152921504606846975L;
        long type = hashLiteralType(value,valueType,datatype,language);
        long intermediate = hashMine(value);
        long hash = type * 31 + intermediate;
        int enumType;

        enumType = ValueHandler.typeOfValue(value,valueType,datatype,language);
        return hash & span | enumType * (span + 1);
    }

    public static long hashLiteralType(String value, String valueType, String datatype, String language)
    {
        if(valueType.equals("LITERAL"))
        {
            if(datatype != null)
            {
                return hashMine(datatype);
            }
            if(language != null)
            {
                return hashMine(language);
            }

        }
        return 0;
    }

    public static long hashMine(String str) {
        try {
            // Create an MD5 digest
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes());

            // Convert byte array to hexadecimal string
            BigInteger bigInteger = new BigInteger(1, bytes);

            // Truncate BigInteger to fit within the range of a long

            return bigInteger.longValue();
        } catch (NoSuchAlgorithmException e) {
            // Handle exception if MD5 algorithm is not available
            e.printStackTrace();
            return 0; // Return 0 as a default value
        }
    }


    //Helper methods
    private String createBNode(){

        StringBuilder bnode = new StringBuilder();
        int id = LoaderGlobals.nextBNodeId++;

        bnode.append(LoaderGlobals.bnodePrefix).append(id);

        if(id == Integer.MAX_VALUE)
            initBNodeParams();

        return bnode.toString();
    }

    public static void initBNodeParams(){
        StringBuilder name = new StringBuilder();
        long msec = System.currentTimeMillis();

        if(msec < LoaderGlobals.lastBNodePrefixUID + 1)
            msec = LoaderGlobals.lastBNodePrefixUID + 1;

        name.append("node").append(msec).append("x");

        LoaderGlobals.bnodePrefix = name.toString();
        LoaderGlobals.nextBNodeId = 1;
    }

    private String getNodeType(Node node){
        if(node.isBlank())
            return "BNODE";
        if(node.isLiteral())
            return "LITERAL";
        if(node.isURI())
            return "URI";
        return null;
    }

    private String getNodeDatatype(Node node){
        if(node.isLiteral())
            return node.getLiteralDatatype().getURI().toString();
        return null;
    }

    private String getNodeLanguage(Node node){
        if(node.isLiteral())
            return node.getLiteralLanguage();
        return null;
    }


    private String createName(String input) {
        String result = "";

        // Define regular expressions
        Pattern pattern1 = Pattern.compile("\\W(\\w*)\\W*$");
        Pattern pattern2 = Pattern.compile("^[^a-zA-Z]*");

        // Find matches using the first regex pattern
        Matcher matcher1 = pattern1.matcher(input);
        if (matcher1.find()) {
            result = matcher1.group(matcher1.groupCount());
        }

        // Replace matches found with the second regex pattern
        Matcher matcher2 = pattern2.matcher(result);
        result = matcher2.replaceAll("");

        // Adjust result length according to requirements
        if (result.isEmpty()) {
            return "triples";
        } else if (result.length() > 16) {
            return result.substring(0, 16);
        } else {
            return result;
        }
    }

    private String escapeCommas(String value){
        // Remove any newline characters from the input string
        value = value.replaceAll("\n", "");

        // Define StringBuilder to store the parsed object
        StringBuilder parsedObjectBuilder = new StringBuilder();

        // Define pattern to search for commas
        Pattern pattern = Pattern.compile(",");
        Matcher matcher = pattern.matcher(value);

        // Initialize variables to track word length and beginning
        int wordLen = 0;
        int wordBegin = 0;

        // Iterate through the object string
        while (matcher.find()) {
            // Append the substring from the last word beginning to the current comma position
            parsedObjectBuilder.append(value, wordBegin, matcher.start());
            // Append backslash before each comma
            parsedObjectBuilder.append("\\,");
            // Update word beginning position
            wordBegin = matcher.end();
        }

        // Append the remaining part of the string after the last comma
        parsedObjectBuilder.append(value.substring(wordBegin));

        // Convert StringBuilder to a String
        return parsedObjectBuilder.toString();
    }

    private long convertDateTimeToMilliseconds(String dateTime) {
        // Remove ":" characters from the date-time string
        dateTime = dateTime.replace(":", "");

        // Remove "-" characters from the date-time string
        dateTime = dateTime.replace("-", "");

        // Convert the string to LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime);

        // Convert LocalDateTime to milliseconds since Unix epoch

        return localDateTime.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    // Convert byte array to hexadecimal string
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    private int extractEPSG(String input) {
        // Define the pattern to match the EPSG number
        Pattern pattern = Pattern.compile(".*EPSG/(\\d+).*");

        // Match the pattern against the input string
        Matcher matcher = pattern.matcher(input);

        // Check if a match is found
        if (matcher.find()) {
            // Extract and return the EPSG number
            return Integer.parseInt(matcher.group(1));
        } else {
            // Return a default value if no match is found
            return -1; // Or throw an exception, depending on your requirements
        }
    }
}

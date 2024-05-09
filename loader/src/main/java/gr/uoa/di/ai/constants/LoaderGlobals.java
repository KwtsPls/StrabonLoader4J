package gr.uoa.di.ai.constants;

import gr.uoa.di.ai.types.LabelDatatype;
import gr.uoa.di.ai.types.LabelLang;
import gr.uoa.di.ai.types.PredicateContent;
import org.locationtech.jts.io.OutStream;
import org.locationtech.jts.io.OutputStreamOutStream;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoaderGlobals {
    //Sesame tables
    public static HashMap<String,Integer> bnode_values=new HashMap<>();
    public static HashMap<String,Integer> label_values=new HashMap<>();
    public static HashMap<String,Integer> long_label_values=new HashMap<>();
    public static HashMap<String,Integer> long_uri_values=new HashMap<>();
    public static HashMap<String,Integer> uri_values=new HashMap<>();

    //Lookup tables
    public static HashMap<String,Integer> namespaces=new HashMap<>();

    //Predicate table
    public static HashMap<String, PredicateContent> predicates=new HashMap<>();

    //Tables for duplicates
    public static HashMap<LabelLang,Integer> label_lang=new HashMap<>();
    public static HashMap<LabelDatatype,Integer> label_datatype=new HashMap<>();
    public static HashMap<LabelLang,Integer> long_label_lang=new HashMap<>();
    public static HashMap<LabelDatatype,Integer> long_label_datatype=new HashMap<>();

    //Bnode variables
    public static HashMap<String,String> bnodeAssignments=new HashMap<>();
    public static int nextBNodeId=0;
    public static String bnodePrefix;
    public static long lastBNodePrefixUID = 0;

    public static int[] minIds=new int[16]; //will contain the span of each value type included in the previous enum

    public static int uriNo = 0;
    public static int uriLongNo = 0;
    public static int bnodeNo = 0;
    public static int simpleNo = 0;
    public static int simpleLongNo = 0;
    public static int typedNo = 0;
    public static int typedLongNo = 0;
    public static int numericNo = 0;
    public static int datetimeNo = 0;
    public static int datetimeZonedNo = 0;
    public static int langNo = 0;
    public static int langlongNo = 0;
    public static int xmlNo = 0;
    public static int b14No = 0; //not used currently
    public static int b15No = 0; //not used currently
    public static int b16No = 0; //not used currently


    //Optimization: In subject's case, check whether the URI you encountered is the same as the previous one.
    //In this case: Assign previous id
    public static String previousURI = "";
    public static int previousId;

    //Global variable keeping the Id corresponding to a named-graph. The id of the default graph is 0.
    //If an empty URI is specified, triples are stored in the default graph.
    public static int graphId = 0;

    //File-writer pool for predicate tables
    public static ArrayList<BufferedWriter> streamPool;
    public static int streamCounter = 0;

    //File writers
    public static BufferedWriter hashOutput;
    public static BufferedWriter datatypeOutput;
    public static BufferedWriter languageOutput;
    public static BufferedWriter numericsOutput;
    public static BufferedWriter datetimeOutput;

    //Geo writers
    public static OutStream geoOutput;

    public static void init(){
        streamPool = new ArrayList<>();
        try {
            hashOutput = new BufferedWriter(new FileWriter(LoaderGeneralConstants.CSV_HASH));
            datatypeOutput =  new BufferedWriter(new FileWriter(LoaderGeneralConstants.CSV_DATATYPES));
            languageOutput = new BufferedWriter(new FileWriter(LoaderGeneralConstants.CSV_LANG));
            numericsOutput = new BufferedWriter(new FileWriter(LoaderGeneralConstants.CSV_NUMERIC));
            datetimeOutput = new BufferedWriter(new FileWriter(LoaderGeneralConstants.CSV_DATETIME));
            geoOutput = new OutputStreamOutStream(new FileOutputStream(LoaderGeneralConstants.CSV_GEO_VALUES));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close(){
        try {
            csvOutput(label_values,LoaderGeneralConstants.CSV_LABELS);
            csvOutput(long_label_values,LoaderGeneralConstants.CSV_LONG_LABELS);
            csvOutput(bnode_values,LoaderGeneralConstants.CSV_BNODES);

            csvOutputURI();
            csvOutputLabelLang();
            csvOutputLabelDatatype();

            hashOutput.close();
            datatypeOutput.close();
            languageOutput.close();
            numericsOutput.close();
            datetimeOutput.close();

            for (Map.Entry<String, PredicateContent> entry : predicates.entrySet()) {
                    PredicateContent content = entry.getValue();
                    content.getPredTable().close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static <K, V> void csvOutput(Map<K, V> map, String fileName) throws IOException {
        try (FileWriter writer = new FileWriter(fileName)) {
            printMap(map, ",", writer);
        }
    }

    private static void csvOutputURI() {
        try (FileWriter writer1 = new FileWriter(LoaderGeneralConstants.CSV_URIS)) {
            for (Map.Entry<String, Integer> entry : uri_values.entrySet()) {
                String uri = entry.getKey();
                Integer value = entry.getValue();
                writer1.append(value.toString());
                writer1.append(",");
                writer1.append(escapeLiteral4CSV(uri));
                writer1.append("\n");
            }
        } catch (IOException e) {
            System.out.println("Error writing CSV file: " + e.getMessage());
        }

        try (FileWriter writer2 = new FileWriter(LoaderGeneralConstants.CSV_LONG_URIS)) {
            for (Map.Entry<String, Integer> entry : long_uri_values.entrySet()) {
                String uri = entry.getKey();
                Integer value = entry.getValue();
                writer2.append(value.toString());
                writer2.append(",");
                writer2.append(escapeLiteral4CSV(uri));
                writer2.append("\n");
            }
        } catch (IOException e) {
            System.out.println("Error writing CSV file: " + e.getMessage());
        }
    }

    private static void csvOutputLabelLang() {

        // Output to labels.csv
        FileWriter myfile1 = null;
        try {
            myfile1 = new FileWriter(LoaderGeneralConstants.CSV_LABELS, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Map.Entry<LabelLang, Integer> entry : label_lang.entrySet()) {
            String lbl = entry.getKey().getLabel();
            escapeLiteral4CSV(lbl);

            try {
                myfile1.write(entry.getValue() + "," + lbl + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            myfile1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Output to long_labels.csv
        FileWriter myfile2 = null;
        try {
            myfile2 = new FileWriter(LoaderGeneralConstants.CSV_LONG_LABELS, true);
            for (Map.Entry<LabelLang, Integer> entry : long_label_lang.entrySet()) {
                String lbl = entry.getKey().getLabel();
                escapeLiteral4CSV(lbl);

                myfile2.write(entry.getValue() + "," + lbl + "\n");
            }
            myfile2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void csvOutputLabelDatatype() {

        // Output to labels.csv
        FileWriter myfile1 = null;
        try {
            myfile1 = new FileWriter(LoaderGeneralConstants.CSV_LABELS, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Map.Entry<LabelDatatype, Integer> entry : label_datatype.entrySet()) {
            String lbl = entry.getKey().getLabel();
            escapeLiteral4CSV(lbl);

            try {
                myfile1.write(entry.getValue() + "," + lbl + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            myfile1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Output to long_labels.csv
        FileWriter myfile2 = null;
        try {
            myfile2 = new FileWriter(LoaderGeneralConstants.CSV_LONG_LABELS, true);
            for (Map.Entry<LabelDatatype, Integer> entry : long_label_datatype.entrySet()) {
                String lbl = entry.getKey().getLabel();
                escapeLiteral4CSV(lbl);

                myfile2.write(entry.getValue() + "," + lbl + "\n");
            }
            myfile2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static <K, V> void printMap(Map<K, V> map, String delimiter, FileWriter writer) throws IOException {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            int id = (int) entry.getValue();
            String value = entry.getKey().toString();
            writer.append(Integer.toString(id));
            writer.append(delimiter);
            writer.append(escapeLiteral4CSV(value));
            writer.append("\n");
        }
    }

    private static String escapeLiteral4CSV(String value) {
        int quoteEscPos;

        // Check for special characters
        if ((quoteEscPos = value.indexOf('"')) != -1 ||
                value.indexOf(',') != -1 ||
                value.contains("\r") ||
                value.contains("\n") ||
                value.contains(LoaderGeneralConstants.CSV_NULL_STR)) {

            // Escape QUOTE and ESCAPE characters
            if (quoteEscPos != -1) {
                value = value.replaceAll("\"", "\"\"");
            }

            // Enclose the string in QUOTEs
            value = "\"" + value + "\"";
        }

        return value;
    }
}

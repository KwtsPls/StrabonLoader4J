package gr.uoa.di.ai;

import gr.uoa.di.ai.constants.AllValueTypes;
import gr.uoa.di.ai.constants.LoaderGeneralConstants;
import gr.uoa.di.ai.constants.LoaderGlobals;
import gr.uoa.di.ai.handlers.TripleHandler;
import gr.uoa.di.ai.handlers.ValueHandler;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

import java.io.IOException;
import java.io.InputStream;

public class Loader {
    public static void main(String[] args) {

        if(args.length<3 || args.length%2==0){
            System.out.println("Usage: ./Loader (<FILENAME> <GRAPH-URI>)+");
            System.out.println("Example: ./filler /tmp/file1.nt 'http://example.org/graph1' /tmp/file2.nt 'http://example.org/graph2'");
            System.exit(0);
        }

        String fileName;
        String graphURI;
        long hashValueGraph;

        LoaderGlobals.init();

        //Initializing the minIds structure
        for(int i=0;i<16;i++)
        {
            LoaderGlobals.minIds[i] = i * (LoaderGeneralConstants.SPAN + 1);
        }

        TripleHandler.initBNodeParams();

        for(int i=1;i<args.length;i+=2){
            fileName = args[i];
            graphURI = args[i+1];

            if(graphURI.length()==0){
                LoaderGlobals.graphId=0;
            }
            else{
                /* search for hash value of graphURI */
                Integer uri_values_id = LoaderGlobals.uri_values.get(graphURI);
                Integer long_uri_values_id = LoaderGlobals.long_uri_values.get(graphURI);

                // GraphURI doesn't exist
                if(uri_values_id==null && long_uri_values_id==null){

                    /* hash and assign new id to graphURI */

                    if(ValueHandler.isLong(graphURI)){
                        LoaderGlobals.graphId = ValueHandler.assignId(AllValueTypes.URI_LONG);
                        LoaderGlobals.long_uri_values.put(graphURI,LoaderGlobals.graphId);
                    }
                    else{
                        LoaderGlobals.graphId = ValueHandler.assignId(AllValueTypes.URI);
                        LoaderGlobals.uri_values.put(graphURI,LoaderGlobals.graphId);
                    }

                    hashValueGraph = TripleHandler.hashOf(graphURI,"URI",null,null);
                    try {
                        LoaderGlobals.hashOutput.write(graphURI + "," + hashValueGraph + "\n");
                    } catch (IOException e) {
                        System.out.println("File append for"+LoaderGeneralConstants.CSV_HASH+"failed!!");
                        e.printStackTrace();
                        System.exit(10);
                    }

                }
                else{
                    if(uri_values_id!=null)
                        LoaderGlobals.graphId = uri_values_id;
                    else
                        LoaderGlobals.graphId = long_uri_values_id;
                }
            }

            // Create an empty Jena model
            Model model = ModelFactory.createDefaultModel();

            // Use FileManager to read the .nt file
            InputStream inputStream = FileManager.get().open(fileName);

            if (inputStream == null) {
                throw new IllegalArgumentException("File: " + fileName + " not found");
            }

            // Read the file into the model
            model.read(inputStream, null, "N-TRIPLE");

            // Processing the parsed RDF data
            // Example: print out the triples
            TripleHandler tripleHandler = new TripleHandler();
            model.getGraph().find().forEachRemaining(tripleHandler::handle);
        }

        LoaderGlobals.close();
    }
}

package org.neural.search.opensearch.plugins.ingestion;


import org.neural.search.opensearch.plugins.Helper;

import org.opensearch.ingest.AbstractProcessor;
import org.opensearch.ingest.IngestDocument;
import org.opensearch.ingest.Processor;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import java.util.Map;

import static org.opensearch.ingest.ConfigurationUtils.readIntProperty;

import static org.opensearch.ingest.ConfigurationUtils.readStringProperty;

public final class NeuralSearchIngestionProcessor extends AbstractProcessor {

    public static final String TYPE = "neural-ingestor";
    public static final String VECTOR_FIELD_NAME="semantic_vector";
    public static final String NEURAL_FIELD_NAME="neural";

    private final String field;
    //private final String targetField;
    private final String modelName;
    private final String ingestionType;
    private final int chunkSize;



    NeuralSearchIngestionProcessor(String tag, String description, String field, String modelName, String ingestionType, int chunkSize) {
        super(tag, description);
        this.field = field;
        //this.targetField = targetField;
        this.modelName = modelName;
        this.ingestionType = ingestionType;
        this.chunkSize = chunkSize;
    }


    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {

        String text = ingestDocument.getFieldValue(field, String.class);
        if(ingestionType.equals("nested"))
        {
            List<String> tokens = Helper.toChunks(text,chunkSize);// Arrays.asList(text.split("\\. "));
            List<Map<String,Object>> arr = new ArrayList<Map<String,Object>>();
    
            for(String token:tokens)
            {
                Map<String,Object> val = new HashMap<String,Object>();
                float[] embedding = Helper.getEmbedding(token, modelName);
                //if(embedding==null)
                //System.out.println("Return Null for token "+token);

                val.put("text", token);
                val.put(VECTOR_FIELD_NAME, embedding);
                arr.add(val);
            }

            String fieldName = NEURAL_FIELD_NAME;
            ingestDocument.setFieldValue(fieldName, arr);
        }
        else
        {
            float[] embedding = Helper.getEmbedding(text, modelName);
            ingestDocument.setFieldValue(VECTOR_FIELD_NAME, embedding);   
        }
        ingestDocument.setFieldValue(field, text);
        return ingestDocument;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    String getField() {
        return field;
    }


    public static final class Factory implements Processor.Factory {

        @Override
        public NeuralSearchIngestionProcessor create(Map<String, Processor.Factory> registry, String processorTag,
                                          String description, Map<String, Object> config) throws Exception {

            String field = readStringProperty(TYPE, processorTag, config, "field");
            //String targetField = readStringProperty(TYPE, processorTag, config, "target_field", "neural_vector");
            String modelField = readStringProperty(TYPE, processorTag, config, "model", "default");
            String ingestionType = readStringProperty(TYPE, processorTag, config, "ingestion_type", "single");
            Integer chunkSize = readIntProperty(TYPE, processorTag, config, "chunk_size", 300);

            return new NeuralSearchIngestionProcessor(processorTag, description, field, modelField, ingestionType, chunkSize);
        }
    }

}

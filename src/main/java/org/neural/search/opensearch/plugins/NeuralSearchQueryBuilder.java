package org.neural.search.opensearch.plugins;

//import org.opensearch.knn.index.KNNQuery;
import org.opensearch.knn.index.KNNVectorFieldMapper;


import org.opensearch.knn.plugin.stats.KNNCounter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.Query;
import org.neural.search.opensearch.plugins.services.NeuralException;
import org.opensearch.action.ActionFuture;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.client.Client;
import org.opensearch.common.ParseField;
import org.opensearch.common.ParsingException;
import org.opensearch.common.Strings;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentParser;
import org.opensearch.index.mapper.MappedFieldType;
import org.opensearch.index.query.AbstractQueryBuilder;
import org.opensearch.index.query.QueryShardContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Helper class to build the KNN query
 */
public class NeuralSearchQueryBuilder extends AbstractQueryBuilder<NeuralSearchQueryBuilder> {
    private static Logger logger = LogManager.getLogger(NeuralSearchQueryBuilder.class);
    public static final ParseField TEXT_FIELD = new ParseField("query_text");
    public static final ParseField RETURN_FIELD = new ParseField("return_field");
    public static final ParseField RETURN_TYPE = new ParseField("return_type");
    public static final ParseField RETURN_N = new ParseField("return_n");
    public static final ParseField K_FIELD = new ParseField("k");
    public static final ParseField MODEL = new ParseField("model");
    public static final ParseField RETURN_MODEL = new ParseField("return_model");
    public static int K_MAX = 10000;
    /**
     * The name for the knn query
     */
    public static final String NAME = "neural_query";
    /**
     * The default mode terms are combined in a match query
     */
    private final String fieldName;
    //private final float[] vector;
    String text;
    String returnField;
    String returnType = "NA";
    String returnModelName = "default";
    int returnN = 1;
    private int k = 1;
    String modelName = "default";



    public NeuralSearchQueryBuilder(String fieldName, String text, int k, String modelName, String returnModelName, String returnField, String returnType, int returnN)  {

        validateText(text);
        if (Strings.isNullOrEmpty(fieldName)) {
            throw new IllegalArgumentException("[" + NAME + "] requires fieldName");
        }

        List<String> acceptableReturnTypes = Arrays.asList(new String[]{"qa","sentences"});

        if (returnField != null) {
            if (returnType == null ) {
                throw new IllegalArgumentException("[" + NAME + "] return_type is required with return_field");
            }
            else if (returnType != null && !acceptableReturnTypes.contains(returnType)) {
            
                throw new IllegalArgumentException("[" + NAME + "] invalid return_type");
            }

            if (returnN > 4 || returnN <1) {
                throw new IllegalArgumentException("[" + NAME + "] return_n must be between 1 and 4");
            }
        }

        if (k <= 0) {
            throw new IllegalArgumentException("[" + NAME + "] requires k > 0");
        }
        if (k > K_MAX) {
            throw new IllegalArgumentException("[" + NAME + "] requires k <= " + K_MAX);
        }


        this.fieldName = fieldName;
        this.modelName = modelName;
        //this.vector = textToVec(text,modelName);
        this.k = k;
        this.returnField = returnField;
        this.returnType = returnType;
        this.returnN = returnN;
        this.text = text;
        this.returnModelName = returnModelName;
    }

    /*
    verify that the input text is valid
    */
    private void validateText(String text)
    {

    }

    float[] textToVec(String text, String modelName) throws NeuralException 
    {
        
        //neural.search.BERTFeatureExtractor<Label> extractor;
        float[] result;
        try {
            result = Helper.getEmbedding(text, modelName);
        } catch (Exception e) {
            throw new NeuralException("Failed to call Neural Services API with model ("+modelName+").", e);
        }
        /*try {
            result = AccessController.doPrivileged(new PrivilegedAction<float[]>() {
                public float[] run() {
                    try {
                        float[] result = Helper.getEmbedding(text);
                        return result;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }*/
        
        return result;
    }

    /*private static float[] ObjectsToFloats(List<Object> objs) {
        float[] vec = new float[objs.size()];
        for (int i = 0; i < objs.size(); i++) {
            vec[i] = ((Number) objs.get(i)).floatValue();
        }
        return vec;
    }*/

    /**
     * @param in Reads from stream
     * @throws IOException Throws IO Exception
     */
    public NeuralSearchQueryBuilder(StreamInput in) throws IOException {
        super(in);
        try {
            fieldName = in.readString();
            text = in.readString();
            returnField = in.readOptionalString();
            returnType = in.readOptionalString();
            modelName = in.readOptionalString();
            returnModelName = in.readOptionalString();
            //vector = in.readFloatArray();
            k = in.readOptionalInt();
            returnN = in.readOptionalInt();

        } catch (IOException ex) {
            throw new RuntimeException("[KNN] Unable to create KNNQueryBuilder: " + ex);
        }
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeString(fieldName);
        out.writeString(text);
        out.writeOptionalString(returnField);
        out.writeOptionalString(returnType);
        out.writeOptionalString(modelName);
        out.writeOptionalString(returnModelName);
        //out.writeFloatArray(vector);
        out.writeOptionalInt(k);
        out.writeOptionalInt(returnN);
    }

    public static NeuralSearchQueryBuilder fromXContent(XContentParser parser) throws IOException {
        String fieldName = null;
        String text = null;
        String returnField = null;
        String returnType = "NA";
        int returnN = 1;
        String modelName = "default";
        String returnModelName = "default";
        int k = 1;
        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        
        String queryName = null;
        String currentFieldName = null;
        XContentParser.Token token;
        KNNCounter.KNN_QUERY_REQUESTS.increment();
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                throwParsingExceptionOnMultipleFields(NAME, parser.getTokenLocation(), fieldName, currentFieldName);
                fieldName = currentFieldName;
                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        currentFieldName = parser.currentName();
                    } else if (token.isValue() || token == XContentParser.Token.START_ARRAY) {
                        if (TEXT_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            //converting the input text into an embedding vector
                            text = parser.text();
                        } 
                        else if (RETURN_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            //converting the input text into an embedding vector
                            returnField = parser.text();
                        } 
                        else if (RETURN_TYPE.match(currentFieldName, parser.getDeprecationHandler())) {
                            //converting the input text into an embedding vector
                            returnType = parser.text();
                        } 
                        else if (RETURN_N.match(currentFieldName, parser.getDeprecationHandler())) {
                            //converting the input text into an embedding vector
                            returnN = parser.intValue();
                        } 
                        else if (MODEL.match(currentFieldName, parser.getDeprecationHandler())) {
                            //converting the input text into an embedding vector
                            modelName = parser.text();
                        } 
                        else if (RETURN_MODEL.match(currentFieldName, parser.getDeprecationHandler())) {
                            //converting the input text into an embedding vector
                            returnModelName = parser.text();
                        } 
                        else if (AbstractQueryBuilder.BOOST_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            boost = parser.floatValue();
                        } else if (K_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            k = parser.intValue();
                        } else if (AbstractQueryBuilder.NAME_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            queryName = parser.text();
                        } else {
                            throw new ParsingException(parser.getTokenLocation(),
                                    "[" + NAME + "] query does not support [" + currentFieldName + "]");
                        }
                    } else {
                        throw new ParsingException(parser.getTokenLocation(),
                                "[" + NAME + "] unknown token [" + token + "] after [" + currentFieldName + "]");
                    }
                }
            } else {
                throwParsingExceptionOnMultipleFields(NAME, parser.getTokenLocation(), fieldName, parser.currentName());
                fieldName = parser.currentName();
                text = parser.text();
            }
        }


        NeuralSearchQueryBuilder knnQuery = new NeuralSearchQueryBuilder(fieldName, text, k, modelName, returnModelName, returnField, returnType, returnN);
        knnQuery.queryName(queryName);
        knnQuery.boost(boost);
        return knnQuery;
    }

    

    /**
     * @return The field name used in this query
     */
    public String fieldName() {
        return this.fieldName;
    }

    /**
     * @return Returns the vector used in this query.
     *
    public Object vector() {
        return this.vector;
    }*/

    public int getK() {
        return this.k;
    }

    @Override
    public void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.startObject(fieldName);

        builder.field(TEXT_FIELD.getPreferredName(), text);
        builder.field(K_FIELD.getPreferredName(), k);
        if(this.returnField!=null)
        {
            builder.field(RETURN_FIELD.getPreferredName(), this.returnField);
            builder.field(RETURN_TYPE.getPreferredName(), this.returnType);
            builder.field(RETURN_N.getPreferredName(), this.returnN);
            builder.field(RETURN_MODEL.getPreferredName(), this.returnModelName);
        }
        
        printBoostAndQueryName(builder);
        builder.endObject();
        builder.endObject();
    }

    @Override
    protected Query doToQuery(QueryShardContext context) throws IOException {

        MappedFieldType mappedFieldType = context.fieldMapper(this.fieldName);


        if (!(mappedFieldType instanceof KNNVectorFieldMapper.KNNVectorFieldType)) {
            throw new IllegalArgumentException("Field '" + this.fieldName + "' is not knn_vector type.");
        }

        int dimension = ((KNNVectorFieldMapper.KNNVectorFieldType) mappedFieldType).getDimension();
        
        String indexName = context.index().getName();
        

        /*String tmp = "field: "+ this.fieldName+";\n"+
        "k: "+ k+";\n"+
        "indexName: "+ indexName+";\n"+
        "text: "+ this.text+";\n"+
        "returnField: "+ this.returnField+";\n"+
        "returnN: "+ this.returnN+";\n"+
        "returnModelName: "+ this.returnModelName+";\n"+
        "modelName: "+ this.modelName+";\n"+
        "returnType: "+ this.returnType+";\n";
        System.out.println(tmp);*/

        if(this.modelName == null || this.modelName.equals("default"))
        {
            try
            {
                Client client = context.getClient();
                ActionFuture<GetResponse> i = client.get(new GetRequest("neural_indices_config", indexName));
                if(i.actionGet().isExists())
                {
                    modelName = (String) i.actionGet().getSource().get("model_name");
                }
            }
            catch(Exception e)
            {

            }
            
        }
        
        float[] vector = textToVec(text, modelName);

        if (dimension != vector.length) {
            throw new IllegalArgumentException("Query vector has invalid dimension: " + vector.length +
                    ". Dimension should be: " + dimension);
        }

        
        return new NeuralQuery(this.fieldName, vector, k, indexName, this.text, this.returnField, this.returnType, this.returnN, this.returnModelName);
    }

    @Override
    protected boolean doEquals(NeuralSearchQueryBuilder other) {
        return Objects.equals(fieldName, other.fieldName) &&
                       //Objects.equals(vector, other.vector) &&
                       Objects.equals(k, other.k);
    }

    @Override
    protected int doHashCode() {
        return Objects.hash(fieldName, /*vector,*/ k);
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }
}
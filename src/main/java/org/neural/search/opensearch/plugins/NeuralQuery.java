package org.neural.search.opensearch.plugins;

import org.opensearch.knn.index.KNNQuery;

public class NeuralQuery extends KNNQuery {

    NeuralQueryParams params;

    public NeuralQuery(String field, float[] queryVector, int k, String indexName, String text, String returnField, String returnType, int returnN, String returnModelName) {
        super(field, queryVector, k, indexName);
        this.params = new NeuralQueryParams(text, returnField, returnType, returnN, k, returnModelName);
    }

    public NeuralQueryParams getParams()
    {
        return params;
    }


    
}

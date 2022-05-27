package org.neural.search.opensearch.plugins;

public class NeuralQueryParams {
    String returnField;
    String returnType;
    int returnN, k;
    String text;
    String returnModelName;

    public NeuralQueryParams(String text, String returnField, String returnType, int returnN, int k, String returnModelName) {
        this.text = text;
        this.returnField = returnField;
        this.returnType = returnType;
        this.returnN = returnN;
        this.k = k;
        this.returnModelName = returnModelName;
    }
    public String getReturnField() {
        return returnField;
    }

    public String getReturnType() {
        return returnType;
    }

    public int getReturnN() {
        return returnN;
    }

    public int getK()
    {
        return k;
    }

    public String getText()
    {
        return text;
    }

    public String getReturnModelName() {
        return returnModelName;
    }

    

}

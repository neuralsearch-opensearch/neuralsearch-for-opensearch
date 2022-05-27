package org.neural.search.opensearch.plugins.services;

public class EmbeddingParam {
    String text, model;

    public EmbeddingParam(String text, String model) {
        this.text = text;
        this.model = model;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    
    
}

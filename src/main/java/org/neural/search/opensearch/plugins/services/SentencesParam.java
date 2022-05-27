package org.neural.search.opensearch.plugins.services;

public class SentencesParam {
    String text, context, model;

    public SentencesParam(String text, String context, String model) {
        this.text = text;
        this.context = context;
        this.model = model;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    
    
}

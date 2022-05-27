package org.neural.search.opensearch.plugins.services;

public class QAParam {
    String context, question, model;

    
    public QAParam(String context, String question, String model) {
        this.context = context;
        this.question = question;
        this.model = model;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    
    
    
}

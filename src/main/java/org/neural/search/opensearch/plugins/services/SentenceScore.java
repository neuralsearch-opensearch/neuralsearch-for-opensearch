package org.neural.search.opensearch.plugins.services;

public class SentenceScore {
    String sentence;
    float score;
    public SentenceScore(String sentence, float score) {
        this.sentence = sentence;
        this.score = score;
    }
    public String getSentence() {
        return sentence;
    }
    public void setSentence(String sentence) {
        this.sentence = sentence;
    }
    public float getScore() {
        return score;
    }
    public void setScore(float score) {
        this.score = score;
    }

    
    
}

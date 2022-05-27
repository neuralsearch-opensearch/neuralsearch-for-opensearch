package org.neural.search.opensearch.plugins.services;

public class NeuralAnswer {
    String answer, answerHighlight;
    float score;

    public NeuralAnswer(String answer, String answerHighlight, float score) {
        this.answer = answer;
        this.answerHighlight = answerHighlight;
        this.score = score;
    }

    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    public String getAnswerHighlight() {
        return answerHighlight;
    }
    public void setAnswerHighlight(String answerHighlight) {
        this.answerHighlight = answerHighlight;
    }
    public float getScore() {
        return score;
    }
    public void setScore(float score) {
        this.score = score;
    }
    
}

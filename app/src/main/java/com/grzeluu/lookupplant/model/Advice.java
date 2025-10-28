package com.grzeluu.lookupplant.model;

import java.io.Serializable;

public class Advice implements Serializable {
    String id;
    String answer;
    String question;
    boolean isVerified;
    String authorId;
    long timestamp;

    public Advice() {
    }

    public Advice(String id, String answer, String question, boolean isVerified) {
        this.id = id;
        this.answer = answer;
        this.question = question;
        this.authorId = authorId;
        this.timestamp = timestamp;
        this.isVerified = isVerified;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

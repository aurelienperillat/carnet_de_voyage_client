package com.example.aurel.bdd;

public class Card {
    private long id, tripId;
    private String imgUri, text;

    public  Card(long id, long tripId, String imgUri, String text) {
        this. tripId = tripId;
        this.id = id;
        this.imgUri = imgUri;
        this.text = text;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id= id; }

    public long getTripId() { return tripId; }
    public void setTripId(long tripId) { this.tripId= tripId; }

    public String getImgUri() { return imgUri; }
    public void setImgUri(String imgUri ) { this.imgUri= imgUri; }

    public String getText() { return text; }
    public void setText(String text) { this.text= text; }
}


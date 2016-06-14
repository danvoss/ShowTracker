package com.dvoss;

/**
 * Created by Dan on 6/9/16.
 */
public class Show {
    int id;
    String artist;
    String date;
    String location;
    String notes;
    int userId;


    public Show(int id, String artist, String date, String location, String notes, int userId) {
        this.id = id;
        this.artist = artist;
        this.date = date;
        this.location = location;
        this.notes = notes;
        this.userId = userId;
    }
}

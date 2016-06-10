package com.dvoss;

/**
 * Created by Dan on 6/9/16.
 */
public class Show {
    String creator;
    String artist;
    String date;
    String location;
    String notes;
    int id;

    public Show(String creator, String artist, String date, String location, String notes, int id) {
        this.creator = creator;
        this.artist = artist;
        this.date = date;
        this.location = location;
        this.notes = notes;
        this.id = id;
    }
}

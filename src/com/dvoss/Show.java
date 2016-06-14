package com.dvoss;

/**
 * Created by Dan on 6/9/16.
 */
public class Show {
    int id;
    String creator;
    String artist;
    String date;
    String location;
    String notes;


    public Show(int id, String creator, String artist, String date, String location, String notes) {
        this.id = id;
        this.creator = creator;
        this.artist = artist;
        this.date = date;
        this.location = location;
        this.notes = notes;

    }
}

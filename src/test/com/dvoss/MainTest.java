package com.dvoss;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Dan on 6/14/16.
 */
public class MainTest {

    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTables(conn);
        return conn;
    }

    @Test
    public void testUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Bob", "pw");
        User user = Main.selectUser(conn, "Bob");
        conn.close();
        assertTrue(user != null);
    }

    @Test
    public void testShow() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Bob", "pw");
        Main.insertShow(conn, "Artist", "Date", "Location", "Notes", 1);
        Show show = Main.selectShow(conn, 1);
        conn.close();
        assertTrue(show != null );
        assertTrue(show.creator.equals("Bob"));
    }

    @Test
    public void testShows() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Bob", "pw");
        Main.insertUser(conn, "Charlie", "pw");

        User bob = Main.selectUser(conn, "Bob");
        User charlie = Main.selectUser(conn, "Charlie");

        Main.insertShow(conn, "Artist", "Date", "Location", "Notes", bob.id);
        Main.insertShow(conn, "Kunstler", "Datum", "Ort", "Notizen", charlie.id);
        Main.insertShow(conn, "Band", "Time", "Place", "Comments", charlie.id);

        ArrayList<Show> shows = Main.selectShows(conn);
        conn.close();
        assertTrue(shows.size() == 3);
    }

}
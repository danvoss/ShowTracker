package com.dvoss;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();
    static ArrayList<Show> shows = new ArrayList<>();

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS shows (id IDENTITY, artist VARCHAR, date VARCHAR, location VARCHAR, notes VARCHAR, user_id INT)");
    }

    public static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, password);
        stmt.execute();
    }

    public static User selectUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int id = results.getInt("id");
            String password = results.getString("password");
            return new User(id, name, password);
        }
        return null;
    }

    public static void insertShow(Connection conn, String artist, String date, String location, String notes, int userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO shows VALUES (NULL, ?, ?, ?, ?, ?)");
        stmt.setString(1, artist);
        stmt.setString(2, date);
        stmt.setString(3, location);
        stmt.setString(4, notes);
        stmt.setInt(5, userId);
        stmt.execute();
    }

    public static Show selectShow(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM shows INNER JOIN users ON shows.user_id = users.id WHERE users.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            String creator = results.getString("users.name");
            String artist = results.getString("shows.artist");
            String date = results.getString("shows.date");
            String location = results.getString("shows.location");
            String notes = results.getString("shows.notes");
            return new Show(id, creator, artist, date, location, notes);
        }
        return null;
    }

    public static ArrayList<Show> selectShows(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM shows INNER JOIN users ON shows.user_id = users.id");
        ResultSet results = stmt.executeQuery();
        ArrayList<Show> shows = new ArrayList<>();
        while (results.next()) {
            int id = results.getInt("id");
            String creator = results.getString("users.name");
            String artist = results.getString("shows.artist");
            String date = results.getString("shows.date");
            String location = results.getString("shows.location");
            String notes = results.getString("shows.notes");
            Show show = new Show(id, creator, artist, date, location, notes);
            shows.add(show);
        }
        return shows;
    }

    public static void deleteShow(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM shows WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }

    public static void updateShow(Connection conn, String artist, String date, String location, String notes, int userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE shows SET artist = ?, date = ?, location = ?, notes = ? WHERE user_id = ?");
        stmt.setString(1, artist);
        stmt.setString(2, date);
        stmt.setString(3, location);
        stmt.setString(4, notes);
        stmt.setInt(5, userId);
        stmt.execute();

    }



    public static void main(String[] args) {

        addTestShows();

        Spark.init();
        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();

                    String username = session.attribute("username");
                    String pw = session.attribute("pass");

                    String idStr = request.queryParams("id");
                    int id = 0;
                    if (idStr != null) {
                        id = Integer.valueOf(idStr);
                    }

                    HashMap m = new HashMap();
                    m.put("shows", shows);
                    m.put("username", username);
                    m.put("pass", pw);
                    m.put("id", id);

                    return new ModelAndView(m, "home.html");
                },
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                (request, response) -> {

                    String username = request.queryParams("username");
                    String pw = request.queryParams("pass");
                    if (username == null || pw == null) {
                        Spark.halt("Name or password not sent");
                    }
                    User user = users.get(username);
                    if (user == null) {
                        user = new User(username, pw);
                        users.put(username, user);
                    }
                    else if (!pw.equals(user.password)) {
                        Spark.halt("Wrong password");
                    }
                    Session session = request.session();
                    session.attribute("username", username);

                    response.redirect(("/"));
                    return "";
                }
        );
        Spark.post(
                "/logout",
                (request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/create-show",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        throw new Exception("Not logged in.");
                    }
                    String newArtist = request.queryParams("artist");
                    String newDate = request.queryParams("date");
                    String newLocation = request.queryParams("location");
                    String newNotes = request.queryParams("notes");
                    Show newShow = new Show(shows.size(), username, newArtist, newDate, newLocation, newNotes);
                    shows.add(newShow);
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/delete-show",
                (request, response) -> {
                    int id = Integer.valueOf(request.queryParams("id"));
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        throw new Exception("Not logged in");
                    }
                    Show delShow = shows.get(id);
                    if (!delShow.creator.equals(username)) {
                        throw new Exception("You may only delete shows you created.");
                    }
                    shows.remove(id);
                    int index = 0;
                    for (Show show : shows) {
                        show.id = index;
                        index++;
                    }
                    response.redirect("/");
                    return "";
                }
        );
        Spark.get(
                "/show",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    HashMap m = new HashMap();
                    String id = request.queryParams("id");
                    Show myShow = shows.get(Integer.valueOf(id));
                    m.put("show", myShow);
                    m.put("isOwner", username != null && myShow != null && myShow.creator.equals(username));
                    return new ModelAndView(m, "show.html");
                },
                new MustacheTemplateEngine()
        );
        Spark.get(
                "/update",
                (request, response) -> {
                    HashMap m = new HashMap();
                    String id = request.queryParams("id");
                    Show show = shows.get(Integer.valueOf(id));
                    m.put("show", show);
                    m.put("id", id);
                    m.put("artist", show.artist);
                    m.put("date", show.date);
                    m.put("location", show.location);
                    m.put("notes", show.notes);
                    return new ModelAndView(m, "update.html");
                },
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/update-show",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        throw new Exception("Not logged in");
                    }
                    int id;
                    id = Integer.valueOf(request.queryParams("id"));
                    Show upShow = shows.get(id);
                    if (!upShow.creator.equals(username)) {
                        throw new Exception("You may only update shows you created.");
                    }
                    String artist = request.queryParams("artist");
                    String date = request.queryParams("date");
                    String location = request.queryParams("location");
                    String notes = request.queryParams("notes");
                    upShow = new Show(id, username, artist, date, location, notes);
                    shows.set(id, upShow);

                    response.redirect("/");
                    return "";
                }
        );

    }
    static void addTestShows(){
        shows.add(new Show(0, "dv", "Prince", "Jan 1", "NYC", "great!"));
        shows.add(new Show(1, "dv", "Pearl Jam", "June 30", "Hartford", "cool show"));
    }
}

package com.dvoss;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();
    static ArrayList<Show> shows = new ArrayList<>();

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
                    Show newShow = new Show(username, newArtist, newDate, newLocation, newNotes, shows.size());
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
                    upShow = new Show(username, artist, date, location, notes, id);
                    shows.set(id, upShow);

                    response.redirect("/");
                    return "";
                }
        );

    }
    static void addTestShows(){
        shows.add(new Show("dv", "Prince", "Jan 1", "NYC", "great!", 0));
        shows.add(new Show("dv", "Pearl Jam", "June 30", "Hartford", "cool show", 1));
    }
}

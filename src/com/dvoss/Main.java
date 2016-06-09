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

//                    ArrayList<Show> myShows = new ArrayList<Show>();
//                    for (Show show : shows) {
//                        if (show.creator.equals(username)) {
//                            myShows.add(show);
//                        }
//                    }

                    HashMap m = new HashMap();
                    m.put("shows", shows);
                    m.put("username", username);
                    m.put("pass", pw);
//                    m.put("isOwner", username != null && );
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

                    response.redirect(request.headers("Referer"));
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
                    Show newShow = new Show(username, newArtist, newDate, newLocation, newNotes);
                    shows.add(newShow);
                    response.redirect("/");
                    return "";
                }
        );
    }
    static void addTestShows(){
        shows.add(new Show("me", "Prince", "Jan 1", "NYC", "great!"));
    }
}

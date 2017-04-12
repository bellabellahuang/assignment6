/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Controller.MessageController;
import Entities.Message;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author bellahuang
 */
@ServerEndpoint("/socket")
@ApplicationScoped
public class MessageServer {

    @Inject
    private MessageController messageCtrl;

    DateFormat df = new SimpleDateFormat("EEE MMM dd H:m:s zzz yyyy");

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        messageCtrl = new MessageController();

        JsonObject json = Json.createReader(new StringReader(message)).readObject();
        String output = "";
        List<Session> sessions;

        if (!messageCtrl.containsSession(session)) {
            messageCtrl.addSession(session);
            sessions = messageCtrl.getSessions();
        } else {
            sessions = messageCtrl.getSessions();
        }

        // { "getAll" : true } --> should respond with a JSON Array of the entire List of Messages
        if (json.containsKey("getAll") && json.getBoolean("getAll")) {
            output = messageCtrl.getAllJson().toString();

        } // { "getById" : id } --> should respond with a single JSON Object as specified by the ID
        else if (json.containsKey("getById")) {
            output = messageCtrl.getMessageById(json.getInt("getById")).toString();
        } // { "getFromTo" : [ "startDate", "endDate" ] } --> should respond with a JSON Array of the Messages between startDate and endDate inclusive
        else if (json.containsKey("getFromTo")) {
            JsonArray arr = json.getJsonArray("getFromTo");
            try {
                Date startDate = df.parse(arr.getString(0));
                Date endDate = df.parse(arr.getString(1));
                List<Message> ms = messageCtrl.getMessagesByDate(startDate.toString(), endDate.toString());
                JsonArrayBuilder j = Json.createArrayBuilder();
                for (Message m : ms) {
                    j.add(m.toJSON());
                }
                output = j.build().toString();
            } catch (ParseException ex) {
                output = Json.createObjectBuilder().add("error", "Error parsing dates: " + arr.toString()).build().toString();
            }
        } // { "post" : { ... some Message as below without ID ... } } --> should add the Message to the system and echo back the Message to all connected systems
        else if (json.containsKey("post")) {
            JsonObject post = json.getJsonObject("post");
            try {
                messageCtrl.addMessage(post);
                output = messageCtrl.getAllJson().toString();
            } catch (ParseException ex) {
                output = Json.createObjectBuilder().add("error", "Error passing new post to addMessage: " + post.toString()).build().toString();
            }

        } // { "put" : { ... some Message as below with ID ... } } --> should edit the Message in the system that matches the given ID and echo back { "ok" : true }
        else if (json.containsKey("put")) {

            JsonObject put = json.getJsonObject("put");
            int id = put.getInt("id");
            try {
                messageCtrl.updateMessageById(id, put);
                output = Json.createObjectBuilder().add("ok", true).build().toString();
            } catch (ParseException ex) {
                output = Json.createObjectBuilder().add("error", "Error updating message: " + put.toString()).build().toString();
            }

        } // { "delete" : id } --> should delete the Message in the system that matches the given ID and echo back { "ok" : true }
        else if (json.containsKey("delete")) {
            int id = json.getInt("delete");
            messageCtrl.deleteMessageById(id);
            output = Json.createObjectBuilder().add("ok", true).build().toString();
        } else {
            output = Json.createObjectBuilder().add("error", "Incorrect format").build().toString();
        }

        for (Session s : sessions) {
            RemoteEndpoint.Basic basic = s.getBasicRemote();
            basic.sendText(output);
        }
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {

        if (!messageCtrl.containsSession(session)) {
            messageCtrl.addSession(session);
        }

        Date current = new Date();
        DateFormat df = new SimpleDateFormat("EEE MMM dd H:m:s zzz yyyy");
        String endDate = df.format(current);
        String startDate = df.format(yesterday());
        List<Message> history = messageCtrl.getMessagesByDate(startDate, endDate);
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (Message m : history) {
            arr.add(m.toJSON());
        }
        JsonArray output = arr.build();

        // Sends the JSON Array out as a message history
        RemoteEndpoint.Basic basic = session.getBasicRemote();
        System.out.println("Connected to " + session.getId() + " and sending: " + output.toString());
        basic.sendText(output.toString());
    }

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

}

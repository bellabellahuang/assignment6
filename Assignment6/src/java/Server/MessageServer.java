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
import java.util.logging.Level;
import java.util.logging.Logger;
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
@ServerEndpoint("/messages")
@ApplicationScoped
public class MessageServer {

    @Inject
    private MessageController messageCtrl;

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {

        JsonObject json = Json.createReader(new StringReader(message)).readObject();

        if (!messageCtrl.containsSession(session)) {
            messageCtrl.addSession(session);
        }

        // { "getAll" : true } --> should respond with a JSON Array of the entire List of Messages
        if (json.getBoolean("getAll")) {
            JsonArray messages = messageCtrl.getAllJson();
            for (Session s : messageCtrl.getSessions()) {
                RemoteEndpoint.Basic basic = s.getBasicRemote();
                basic.sendText(messages.toString());
            }
        } // { "getById" : id } --> should respond with a single JSON Object as specified by the ID
        else if (json.containsKey("getById")) {
            int id = json.getInt("getById");
            JsonObject j = messageCtrl.getMessageById(id);
            for (Session s : messageCtrl.getSessions()) {
                RemoteEndpoint.Basic basic = s.getBasicRemote();
                basic.sendText(j.toString());
            }
        } 

        // { "getFromTo" : [ "startDate", "endDate" ] } --> should respond with a JSON Array of the Messages between startDate and endDate inclusive
        else if (json.containsKey("getFromTo")) {
            json.get("getFromTo");
        } 

        // { "post" : { ... some Message as below without ID ... } } --> should add the Message to the system and echo back the Message to all connected systems
        else if (json.containsKey("post")) {
            try {
                JsonObject post = json.getJsonObject("post");
                messageCtrl.addMessage(post);
                for (Session s : messageCtrl.getSessions()) {
                    RemoteEndpoint.Basic basic = s.getBasicRemote();
                    basic.sendText(post.toString());
                }
            } catch (ParseException ex) {
                Logger.getLogger(MessageServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 

        // { "put" : { ... some Message as below with ID ... } } --> should edit the Message in the system that matches the given ID and echo back { "ok" : true }
        else if (json.containsKey("put")) {
            try {
                JsonObject put = json.getJsonObject("put");
                int id = put.getInt("id");
                messageCtrl.updateMessageById(id, put);
                for (Session s : messageCtrl.getSessions()) {
                    RemoteEndpoint.Basic basic = s.getBasicRemote();
                    basic.sendText("{\"ok:\"true");
                }
            } catch (ParseException ex) {
                Logger.getLogger(MessageServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } // { "delete" : id } --> should delete the Message in the system that matches the given ID and echo back { "ok" : true }
        else if (json.containsKey("delete")) {
            int id = json.getInt("delete");
            messageCtrl.deleteMessageById(id);
            for (Session s : messageCtrl.getSessions()) {
                RemoteEndpoint.Basic basic = s.getBasicRemote();
                basic.sendText("{\"ok:\"true");
            }
        } else {
            for (Session s : messageCtrl.getSessions()) {
                RemoteEndpoint.Basic basic = s.getBasicRemote();
                basic.sendText("{\"error\" : \"Some meaningful error message.\" }");
            }
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

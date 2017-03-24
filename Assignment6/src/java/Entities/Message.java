/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;

/**
 *
 * @author bellahuang
 */
public class Message {
    private int id;
    private String title;
    private String contents;
    private String author;
    private Date senttime;

    public Message() {
        
    }
    
    public Message(int id, String title, String contents, String author, Date senttime) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.author = author;
        this.senttime = senttime;
    }
    
    public Message(int id, String title, String contents, String author) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.author = author;
        this.senttime = new Date();
    }
    
    /**
     * get each attribute value from JSON object
     * @param json 
     */
    public Message(JsonObject json){
        try {
            id = json.getInt("id");
            title = json.getString("title");
            contents = json.getString("contents");
            author = json.getString("author");
            // convert String to Date type
            String senttime = json.getString("senttime");
            DateFormat df = new SimpleDateFormat("EEE MMM dd H:m:s zzz yyyy");
            this.senttime = df.parse(senttime);
        } catch (ParseException ex) {
            Logger.getLogger(Message.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getSenttime() {
        return senttime;
    }

    public void setSenttime(Date senttime) {
        this.senttime = senttime;
    }
    
    /**
     * convert each message to JSON format
     * @return JSON
     */
    public JsonObject toJSON(){
        return Json.createObjectBuilder()
                .add("id", id)
                .add("title", title)
                .add("contents", contents)
                .add("author", author)
                .add("senttime", senttime.toString())
                .build();
    }
    
}


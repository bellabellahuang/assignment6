/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import Entities.Message;
import java.sql.*;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.websocket.Session;

/**
 *
 * @author bellahuang
 */
@ApplicationScoped
public class MessageController {

    private List<Message> messages = new ArrayList<>();
    private List<Session> sessions = new ArrayList<>();
    DateFormat df = new SimpleDateFormat("EEE MMM dd H:m:s zzz yyyy");

    /**
     * initialize the messages list
     */
    public MessageController() {
        try {
            refresh();
        } catch (SQLException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void refresh() throws SQLException {
        try {
            java.util.Date date = new java.util.Date();
            Timestamp timestamp;
            messages = new ArrayList<>();
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM messages");
            while (rs.next()) {
                timestamp = rs.getTimestamp("senttime");
                if(timestamp != null) date = new java.util.Date(timestamp.getTime());
                Message m = new Message(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("contents"),
                        rs.getString("author"),
                        date
                );
                messages.add(m);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
            messages = new ArrayList<>();
        }
    }

    public List<Message> getMessages() {
        return messages;
    }
    
    public JsonArray getAllJson(){
        JsonArrayBuilder json = Json.createArrayBuilder();
        for (Message m : messages) {
            json.add(m.toJSON());
        }
        return json.build();
    }

    /**
     * get a Message object by a specific id
     *
     * @param id
     * @return Message
     */
    public JsonObject getMessageById(int id) {
        for (Message m : messages) {
            if (m.getId() == id) {
                return m.toJSON();
            }
        }
        return null;
    }

    /**
     * delete the Message specified by id
     *
     * @param id
     * @return "200 OK" when it is deleted successfully
     */
    public String deleteMessageById(int id) {
        String result = "";
        try {
            Connection conn = getConnection();
            String query = "DELETE FROM messages WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            refresh();
            result = "200 OK";
        } catch (SQLException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * update the Message specified by id
     *
     * @param id
     * @param j
     * @return the updated Message
     */
    public JsonObject updateMessageById(int id, JsonObject j) throws ParseException {
        Date d = new Date();
        if (j.containsKey("senttime")){
            d = df.parse(j.getString("senttime"));
        }
        java.sql.Timestamp sql = new java.sql.Timestamp(d.getTime());
        try {
            Connection conn = getConnection();
            String query = "UPDATE messages SET title = ?, contents = ?, author = ?, senttime = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, j.getString("title"));
            pstmt.setString(2, j.getString("contents"));
            pstmt.setString(3, j.getString("author"));
            pstmt.setTimestamp(4, sql);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
            refresh();
        } catch (SQLException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return getMessageById(id);

    }

    /**
     * add a new Message into the Message List
     *
     * @param j
     * @return the Message List of all Message
     * @throws java.text.ParseException
     */
    public List<Message> addMessage(JsonObject j) throws ParseException {
        Date senttime = new Date();
        if (j.containsKey("senttime")) {
            senttime = df.parse(j.getString("senttime"));
        }
        java.sql.Timestamp sql = new java.sql.Timestamp(senttime.getTime());
        try {
            Connection conn = getConnection();
            String query = "INSERT INTO messages(title, contents, author, senttime) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, j.getString("title"));
            pstmt.setString(2, j.getString("contents"));
            pstmt.setString(3, j.getString("author"));
            pstmt.setTimestamp(4, sql);
            pstmt.executeUpdate();
            refresh();
        } catch (SQLException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return messages;
    }

    /**
     * get a List of Message in range of a specific period
     *
     * @param startDate
     * @param endDate
     * @return a List of Message
     */
    public List<Message> getMessagesByDate(String startDate, String endDate) {
        // declare a new List to store Message in a specific period
        List<Message> messagesByDate = new ArrayList<>();
        // loop through each Message in the Message List to compare with the specific date
        for (Message m : messages) {
            try {
                // add the Message to the new List if it matched the specific date period
                if (m.getSenttime().after(df.parse(startDate))
                        && m.getSenttime().before(df.parse(endDate))
                        || m.getSenttime().equals(df.parse(startDate))
                        || m.getSenttime().equals(df.parse(endDate))) {
                    messagesByDate.add(m);
                }
            } catch (ParseException ex) {
                Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return messagesByDate;
    }

    // connect to database
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/blog", "root", "");
            return conn;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public void addSession(Session s){
        sessions.add(s);
    }
    
    public List<Session> getSessions(){
        return sessions;
    }
    
    public boolean containsSession(Session s){
        return sessions.contains(s);
    }
    

}

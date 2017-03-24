/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Service;

import Controller.MessageController;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import Entities.Message;
import java.text.ParseException;

/**
 *
 * @author bellahuang
 */
@Path("/messages")
@ApplicationScoped
public class MessageService {
    
    private MessageController messagesList = new MessageController();
    
    @GET
    @Produces("application/json")
    public Response getAllMessages() {
        JsonArrayBuilder json = Json.createArrayBuilder();
        for (Message m : messagesList.getMessages()) {
            json.add(m.toJSON());
        }
        return Response.ok(json.build().toString()).build();
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public JsonObject getMessageById(@PathParam("id") int id){
        return messagesList.getMessageById(id);
    }
    
    @GET
    @Path("/{startDate}/{endDate}")
    @Produces("application/json")
    public Response getMessageByDate(@PathParam("startDate") String startDate, @PathParam("endDate") String endDate){
        List<Message> rangeMessages = messagesList.getMessagesByDate(startDate, endDate);
        JsonArrayBuilder json = Json.createArrayBuilder();
        for (Message m : rangeMessages) {
            json.add(m.toJSON());
        }
        return Response.ok(json.build().toString()).build();
    }
    
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response addMessages(JsonObject j) throws ParseException{
        messagesList.addMessage(j);
        return getAllMessages();
    }
    
    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    public JsonObject updateMessageById(@PathParam("id") int id, JsonObject j) throws ParseException{
        return messagesList.updateMessageById(id, j);
    }
    
    @DELETE
    @Path("/{id}")
    @Produces("text/plain")
    public String deleteMessageById(@PathParam("id") int id){
        return messagesList.deleteMessageById(id);
    }

}



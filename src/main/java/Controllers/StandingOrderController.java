package Controllers;

import Server.main;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.lang.System.out;

@Path("StandingOrders/")
public class StandingOrderController{


    @GET
    @Path("list/{accountID}")
    @Produces(MediaType.APPLICATION_JSON)

    //This method returns all StandingOrders that relate to an account
    public String search(@PathParam("accountID") Integer searchID){
        System.out.println("StandingOrders/list/" + searchID);

        try{
            //Error is thrown if accountID not entered
            if (searchID == null) throw new Exception("No account exists");
            JSONArray list = new JSONArray();

            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM StandingOrders WHERE AccountID = ?");
            ps.setInt(1,searchID);
            ResultSet result = ps.executeQuery();

            while(result.next()){
                JSONObject item = new JSONObject();
                item.put("OrderID", result.getInt(1));
                item.put("AccountID", result.getInt(2));
                item.put("CategoryID", result.getInt(3));
                item.put("Amount", result.getInt(4));
                item.put("Duration", result.getInt(5));
                item.put("LastPaid", result.getInt(6));
                list.add(item);
            }
            return list.toString();//Returns all of the details of the Standing Orders

        } catch (Exception e){
            System.out.println("Database error: " + e.getMessage());
            return "{\"error\": \"Unable to list items, please see server console for more info.\"}";
        }
    }



    @POST
    @Path("new")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)

    //This creates a new standing order for the client's account
    public String insert(@FormDataParam("accountID") int accountID, @FormDataParam("categoryID") int categoryID, @FormDataParam("amount") int amount,
                         @FormDataParam("duration") int duration, @FormDataParam("lastPaid") String lastPaid){

        try{
            out.println("/StandingOrders/new");

            //The amount of days must be a positive integer as the standing order can only happen at most once per day
            if(duration<=0) throw new Exception("Input duration invalid");

            //Creates the new StandingOrder for the user
            PreparedStatement ps = main.db.prepareStatement("INSERT INTO StandingOrders (OrderID, AccountID, CategoryID, Amount, Duration, LastPaid) VALUES (?,?,?,?,?,?)");

            ps.setString(1,null);//auto-increments the primary key
            fillColumn(accountID, categoryID, amount, duration, lastPaid, ps, 1);//fills in the ps
            ps.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error when inputting standing order into database, error code\n" + e.getMessage());
            return "{\"error\": \"Unable to create new item, please see server console for more info.\"}";
        }
    }



    @POST
    @Path("edit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    //This method edits an existing standing order which will affect future iterations of it.
    public String update(@FormDataParam("orderID") int orderID, @FormDataParam("accountID") int accountID, @FormDataParam("categoryID") int categoryID, @FormDataParam("amount") int amount,
                         @FormDataParam("duration") int duration, @FormDataParam("lastPaid") String lastPaid){
        try{

            System.out.println("StandingOrders/edit id = " + orderID);

            PreparedStatement ps = main.db.prepareStatement("UPDATE StandingOrders SET AccountID = ?, CategoryID = ?, Amount = ?, Duration = ?, LastPaid = ? WHERE OrderID = ?");
            fillColumn(accountID, categoryID, amount, duration, lastPaid, ps,0);
            ps.setInt(6,orderID);
            ps.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error updating user, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to update item, please see server console for more info.\"}";
        }
    }

    @POST
    @Path("delete")
    /*The reason that this uses form data instead of having the data be sent via the API path in the
      path name is that the form is more secure*/
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String delete(@FormDataParam("orderID") Integer searchID){
        try{
            //This throws when there has been no categoryID entered
            if (searchID == null) throw new Exception("One or more form data parameters are missing in the HTTP request.");

            out.println("StandingOrders/delete id = " + searchID);

            PreparedStatement ps = main.db.prepareStatement("DELETE FROM StandingOrders WHERE OrderID = ?");
            ps.setInt(1,searchID);
            ps.execute();

            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error deleting Standing Order, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to delete item, please see server console for more info.\"}";
        }
    }
    
    /*This method is used to efficiently fill the ps,
    as many API paths have nearly identical code within the class
    when filling in prepared statement*/
    private static void fillColumn(int accountID, int categoryID, int amount, int duration, String lastPaid, PreparedStatement ps,int column) throws SQLException {
        ps.setInt(1+column, accountID);//Done as column numbers are one off in difference between update and insert SQL statements
        ps.setInt(2+column, categoryID);
        ps.setInt(3+column,amount);
        ps.setInt(4+column,duration);
        ps.setString(5+column,lastPaid);
    }

}

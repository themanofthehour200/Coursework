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
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

@Path("StandingOrders/")
public class StandingOrderController{

    //This is the method for selecting all rows in the table of Users
    //This method is mainly just used for testing purposes, as this is easier than manually having to check the Accounts table after each applicable test
    //This method returns all budgets available to the user
    @GET
    @Path("list/{accountID}")
    @Produces(MediaType.APPLICATION_JSON)

    //This returns a specific user's details
    public String search(@PathParam("accountID") Integer searchID){
        System.out.println("StandingOrders/list/" + searchID);

        try{
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
            return list.toString();

        } catch (Exception e){
            System.out.println("Database error: " + e.getMessage());
            return "{\"error\": \"Unable to list items, please see server console for more info.\"}";
        }
    }


//This creates a new standing order for the client's account
    @POST
    @Path("new")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)

    public String insert(@FormDataParam("accountID") int accountID, @FormDataParam("categoryID") int categoryID, @FormDataParam("amount") int amount,
                         @FormDataParam("duration") int duration, @FormDataParam("lastPaid") String lastPaid){

        try{
            out.println("/StandingOrders/new");

            if(duration<=0) throw new Exception("Input duration invalid");

            //Creates the new StandingOrder for the user
            PreparedStatement ps = main.db.prepareStatement("INSERT INTO StandingOrders (OrderID, AccountID, CategoryID, Amount, Duration, LastPaid) VALUES (?,?,?,?,?,?)");

            ps.setString(1,null);//auto-increments the primary key
            fillColumn(accountID, categoryID, amount, duration, lastPaid, ps, 1);
            ps.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error when inputting budget into database, error code\n" + e.getMessage());
            return "{\"error\": \"Unable to create new item, please see server console for more info.\"}";
        }
    }



    public static void delete(int searchID){
        try{
            PreparedStatement ps = main.db.prepareStatement("DELETE FROM StandingOrders WHERE OrderID = ?");
            ps.setInt(1,searchID);
            ps.execute();

            out.println("Order number " + searchID + " was deleted successfully");

        } catch (Exception e){
            out.println("Error deleting order, error message:\n" + e.getMessage());
        }
    }

    @POST
    @Path("edit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
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

    /* removes the duplicate code of the data entry into the SQL statement for update() and add(), as there code was very similar */
    private static void fillColumn(int accountID, int categoryID, int amount, int duration, String lastPaid, PreparedStatement ps,int column) throws SQLException {
        ps.setInt(1+column, accountID);//Done as column numbers are one off in difference between update and insert SQL statements
        ps.setInt(2+column, categoryID);
        ps.setInt(3+column,amount);
        ps.setInt(4+column,duration);
        ps.setString(5+column,lastPaid);
    }

}

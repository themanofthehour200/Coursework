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

@Path("Budgets/")
public class BudgetsController{

    @POST
    @Path("new")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)

    public String insert(@FormDataParam("userID") int userID, @FormDataParam("categoryID") int categoryID, @FormDataParam("amount") int amount,
                         @FormDataParam("balance") int balance, @FormDataParam("duration") int duration, @FormDataParam("dateStarted") String dateStarted){

        try{
            out.println("/Budgets/new");

            /*This prevents a user from creating multiple budgets that they have access to that have
              the same names as each other*/
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Budgets WHERE UserID = ? AND CategoryID = ?");
            ps.setInt(1,userID);
            ps.setInt(2,categoryID);
            ResultSet result = ps.executeQuery();
            if(result.next()) throw new Exception("Cannot create duplicate budgets");

            //Creates the new budget for the user
            PreparedStatement ps2 = main.db.prepareStatement("INSERT INTO Budgets (BudgetID, UserID, CategoryID, Amount, Balance, Duration, DateStarted) VALUES (?,?,?,?,?,?,?)");

            ps2.setString(1,null);//auto-increments the primary key
            fillColumn(userID, categoryID, amount, balance, duration, dateStarted, ps2, 1);
            ps2.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error when inputting budget into database, error code\n" + e.getMessage());
            return "{\"error\": \"Unable to create new item, please see server console for more info.\"}";
        }
    }

    @POST
    @Path("delete")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    //This method is used to delete a budget
    public String delete(@FormDataParam("budgetID") Integer searchID){
        try{
            //Error is thrown if budgetID isn't present
            if (searchID == null) throw new Exception("Search ID is invalid in HTTP request");

            out.println("Budgets/delete id = " + searchID);

            PreparedStatement ps = main.db.prepareStatement("DELETE FROM Budgets WHERE UserID = ?");
            ps.setInt(1,searchID);
            ps.execute();

            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error deleting budget, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to delete item, please see server console for more info.\"}";
        }
    }


    @POST
    @Path("edit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    //This method updates an already created budget
    public String update(@FormDataParam("budgetID") int budgetID, @FormDataParam("userID") int userID, @FormDataParam("categoryID") int categoryID, @FormDataParam("amount") int amount,
                         @FormDataParam("balance") int balance, @FormDataParam("duration") int duration, @FormDataParam("dateStarted") String dateStarted){
        try{

            System.out.println("Budgets/edit id = " + budgetID);


            PreparedStatement ps = main.db.prepareStatement("UPDATE Budgets SET UserID = ?, CategoryID = ?, Amount = ?, Balance = ?, Duration = ?, DateStarted = ? WHERE BudgetID = ?");
            fillColumn(userID, categoryID, amount, balance, duration, dateStarted, ps,0);//Fills in the ps
            ps.setInt(7,budgetID);
            ps.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error updating user, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to update item, please see server console for more info.\"}";
        }
    }


    //This method returns all budgets available to the user
    @GET
    @Path("list/{userID}")
    @Produces(MediaType.APPLICATION_JSON)

    //This returns a specific user's details
    public String search(@PathParam("userID") Integer searchID){
        System.out.println("Budgets/list/" + searchID);

        try{
            if (searchID == null) throw new Exception("No user exists");
            JSONArray list = new JSONArray();

            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Budgets WHERE UserID = ?");
            ps.setInt(1,searchID);
            ResultSet result = ps.executeQuery();

            while(result.next()){
                JSONObject item = new JSONObject();
                item.put("BudgetID", result.getInt(1));
                item.put("UserID", result.getInt(2));
                item.put("CategoryID", result.getInt(3));
                item.put("Amount", result.getInt(4));
                item.put("Balance", result.getInt(5));
                item.put("Duration", result.getInt(6));
                item.put("DateStarted", result.getString(7));
                list.add(item);
            }
            return list.toString();

        } catch (Exception e){
            System.out.println("Database error: " + e.getMessage());
            return "{\"error\": \"Unable to list items, please see server console for more info.\"}";
        }
    }

    /*This method is used to efficiently fill the ps,
    as many API paths have nearly identical code within the class
    when filling in prepared statement*/
    private static void fillColumn(int userID, int categoryID, int amount, int balance, int duration, String dateStarted, PreparedStatement ps,int column) throws SQLException {
        ps.setInt(1+column, userID);
        ps.setInt(2+column, categoryID);
        ps.setInt(3+column,amount);
        ps.setInt(4+column,balance);
        ps.setInt(5+column,duration);
        ps.setString(6+column,dateStarted);
    }
}

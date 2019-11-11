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

@Path("Transactions/")
public class TransactionsController{

    @POST
    @Path("create")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)

    public String insert(@FormDataParam("categoryID") int categoryID, @FormDataParam("date") String date, @FormDataParam("accountID") int accountID, @FormDataParam("balanceChange") int balanceChange,
                         @FormDataParam("description") String description, @FormDataParam("standingOrderID") int standingOrderID){

        try{
            PreparedStatement ps = main.db.prepareStatement("INSERT INTO Transactions (TransactionID, AccountID, BalanceChange, CategoryID," +
                    " Description, Date, StandingOrderID) VALUES (?,?,?,?,?,?,?)"); //This adds the transaction to the transaction database

            //makes sure that a transaction with a value of zero isn't done, as this is pointless and may lead to errors
            if (balanceChange == 0) throw new Exception("Transaction cannot be for a value of 0");

            out.println("Transactions/create");

            ps.setString(1,null);//auto-increments the primary key
            fillColumn(accountID,balanceChange,categoryID,description,date,standingOrderID,ps,1);

            ps.executeUpdate();

            //This gets the account balance for the account the transaction is relating to
            PreparedStatement ps2 = main.db.prepareStatement("SELECT Balance FROM Accounts WHERE AccountID = ?");

            JSONObject item = new JSONObject();

            ps2.setInt(1,accountID); //The user with the specific account ID is searched for
            ResultSet result = ps2.executeQuery();

            //This updates the account balance to reflect the transaction

            PreparedStatement ps3 =  main.db.prepareStatement("UPDATE Accounts SET Balance = ? WHERE AccountID = ?");
            ps3.setInt(1,(result.getInt(1)-balanceChange));
            ps3.setInt(2,accountID);
            ps3.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error when inputting user into database, error code\n" + e.getMessage());
            return "{\"error\": \"Unable to create new item, please see server console for more info.\"}";
        }
    }

    @GET
    @Path("view/{id}")
    @Produces(MediaType.APPLICATION_JSON)

    //This returns a specific user's details
    public String search(@PathParam("id") Integer searchID){
        try {
            if (searchID == null) {
                throw new Exception("Thing's 'id' is missing in the HTTP request's URL.");
            }

            System.out.println("Transactions/view/" + searchID);
            JSONArray list = new JSONArray();


            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Transactions WHERE AccountID = ?");

            ps.setInt(1,searchID); //The user with the specific account ID is searched for

            ResultSet result = ps.executeQuery();

            while (result.next()) {
                JSONObject item = new JSONObject();
                item.put("TransactionID", result.getInt(1));
                item.put("AccountID", result.getInt(2));
                item.put("BalanceChange", result.getInt(3));
                item.put("CategoryID", result.getInt(4));
                item.put("Description", result.getString(5));
                item.put("Date", result.getString(6));
                item.put("StandingOrderID", result.getInt(7));
                list.add(item);
            }
            return list.toString();

        }
        catch (Exception e){
            out.println("Error searching database 'Transactions', error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to get item, please see server console for more info.\"}";

        }
    }

    @POST
    @Path("search")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)//Jersey turns this into an HTTP request handler
    public String search(@FormDataParam("categoryID") int categoryID, @FormDataParam("date") String date, @FormDataParam("accountID") int accountID){

        System.out.println("Transactions/search");
        JSONArray list = new JSONArray();

        try{
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Transactions WHERE AccountID = ? AND Date = ? OR CategoryID = ?");
            ps.setInt(1,accountID);
            ps.setString(2,date);
            ps.setInt(3,categoryID);
            ResultSet result = ps.executeQuery();

            boolean added = false;//This determines whether any transactions with the search criteria have been found
            while (result.next()) {
                added = true;
                JSONObject item = new JSONObject();
                item.put("TransactionID", result.getInt(1));
                item.put("AccountID", result.getInt(2));
                item.put("BalanceChange", result.getInt(3));
                item.put("CategoryID", result.getInt(4));
                item.put("Description", result.getString(5));
                item.put("Date", result.getString(6));
                item.put("StandingOrderID", result.getInt(7));
                list.add(item);
            }
            if (added) return list.toString(); //If transactions found then return them
            else throw new Exception("No transactions with search criteria found"); //if none have been found then give an error

        } catch (Exception e){
            System.out.println("Database error: " + e.getMessage());
            return "{\"error\": \"Unable to list items, please see server console for more info.\"}";
        }
    }

    public static List selectAll(){
        try{
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Transactions");
            ResultSet result = ps.executeQuery();

            int count = 0;
            List<List<String>> output = new ArrayList<List<String>>(); //This is a List of ArrayLists. This is what is returned.
            //An ArrayList is used instead of an array as it is mutatable and we don't know how many rows there are in the table

            while(result.next()){
                output.add(new ArrayList<String>());            //A new arraylist is created within the overall output List
                output.get(count).add(Integer.toString(result.getInt(1)));      //The value is added in to the current ArrayList within output
                output.get(count).add(Integer.toString(result.getInt(2)));
                output.get(count).add(Integer.toString(result.getInt(3)));
                output.get(count).add(result.getString(result.getInt(4)));
                output.get(count).add(result.getString(5));
                output.get(count).add(result.getString(6));
                output.get(count).add(Integer.toString(result.getInt(7)));
                out.println(output.get(count)); //To be removed once testing phase one is done
                count++;
            }
            return output;

        } catch (Exception e){
            out.println("Error reading database 'Transactions', error message:\n" + e.getMessage());
            return null;
        }
    }

    //This returns a specific accounts details, allowing the user to check their balance etc.
    public static List search(int searchID){
        try {
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Transactions WHERE TransactionID = ?");
            ps.setInt(1,searchID); //The user with the specific account ID is searched for
            ResultSet result = ps.executeQuery();

            ArrayList<String> output = new ArrayList<String>(1);
            output.add(Integer.toString(result.getInt(1)));      //The value is added in to the current ArrayList within output
            output.add(Integer.toString(result.getInt(2)));
            output.add(Integer.toString(result.getInt(3)));
            output.add(result.getString(result.getInt(4)));
            output.add(result.getString(5));
            output.add(result.getString(6));
            output.add(Integer.toString(result.getInt(7)));
            output.add(Integer.toString(result.getInt(8))); //THIS IS AN ERROR DELETE THIS LINE DURING TESTING

            out.println(output);
            return output;

        }
        catch (Exception e){
            out.println("Error searching database 'Transactions', error message:\n" + e.getMessage());
            return null;
        }

    }

    public static void insert(int accountID, int balanceChange, int categoryID, String description, String date, int standingOrderID){

        try{
            PreparedStatement ps = main.db.prepareStatement("INSERT INTO Transactions (TransactionID, AccountID, BalanceChange, CategoryID, Description, Date, StandingOrderID ) VALUES (?,?,?,?,?,?,?)");

            ps.setString(1,null);
            fillColumn(accountID, balanceChange, categoryID, description, date, standingOrderID, ps,1);

            ps.executeUpdate();

        } catch (Exception e){
            out.println("Error when inputting transaction into database, error code\n" + e.getMessage());
        }
    }

    public static void delete(int searchID){
        try{
            PreparedStatement ps = main.db.prepareStatement("DELETE FROM Transactions WHERE TransactionID = ?");
            ps.setInt(1,searchID);
            ps.execute();

            out.println("Transaction number" + searchID + "was deleted successfully");

        } catch (Exception e){
            out.println("Error deleting transaction, error message:\n" + e.getMessage());
        }
    }

    public static void update(int transactionID, int accountID, int balanceChange, int categoryID, String description, String date, int standingOrderID){
        try{
            PreparedStatement ps = main.db.prepareStatement("UPDATE Transactions SET AccountID = ?, BalanceChange = ?, CategoryID = ?, Description = ?, Date = ?, StandingOrderID = ? WHERE TransactionID = ?");

            fillColumn(accountID, balanceChange, categoryID, description, date, standingOrderID, ps,0);
            ps.setInt(7,transactionID);

            ps.executeUpdate();

        } catch (Exception e){
            out.println("Error updating user, error message:\n" + e.getMessage());
        }
    }

    /* removes the duplicate code of the data entry into the SQL statement for update() and add(), as there code was very similar */
    private static void fillColumn(int accountID, int balanceChange, int categoryID, String description, String date, int standingOrderID, PreparedStatement ps, int column) throws SQLException {

        ps.setInt(1+column, accountID);
        ps.setInt(2+column,balanceChange);
        ps.setInt(3+column,categoryID);
        ps.setString(4+column,description);
        ps.setString(5+column,date);
        ps.setInt(6+column,standingOrderID);
    }

}

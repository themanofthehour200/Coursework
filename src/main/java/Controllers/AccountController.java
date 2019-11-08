package Controllers;

import Server.main;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

@Path("Accounts/")
public class AccountController{

    //This is the method for selecting all rows in the table of Users
    //This method is mainly just used for testing purposes, as this is easier than manually having to check the Accounts table after each applicable test
    public static List selectAll() {
        try {
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Accounts");
            ResultSet result = ps.executeQuery();

            int count = 0;
            List<List<String>> output = new ArrayList<List<String>>(); //This is a List of ArrayLists. This is what is returned.
            //An ArrayList is used instead of an array as it is mutatable and we don't know how many rows there are in the table

            while (result.next()) {
                output.add(new ArrayList<String>());            //A new arraylist is created within the overall output List
                output.get(count).add(Integer.toString(result.getInt(1)));      //The value is added in to the current ArrayList within output
                output.get(count).add(result.getString(2));
                output.get(count).add(Integer.toString(result.getInt(3)));
                output.get(count).add(result.getString(4));
                out.println(output.get(count));
                count++;
            }
            return output;

        } catch (Exception e) {
            out.println("Error reading database, error message:\n" + e.getMessage());
            return null;
        }
    }

    //This returns a specific accounts details, allowing the user to check their balance etc.
    public static List search(int accountID) {
        try {
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Accounts WHERE AccountID = ?");
            ps.setInt(1, accountID); //The account with the specific account ID is searched for
            ResultSet result = ps.executeQuery();

            ArrayList<String> output = new ArrayList<String>(1);
            output.add(Integer.toString(result.getInt(1)));      //The value is added in to the current ArrayList within output
            output.add(result.getString(2));
            output.add(Integer.toString(result.getInt(3)));
            output.add(result.getString(4));

            out.println(output);
            return output;

        } catch (Exception e) {
            out.println("Error searching database, error message:\n" + e.getMessage());
            return null;
        }

    }

    @POST
    @Path("new")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)

    public String insert(@FormDataParam("userID") int userID, @FormDataParam("accountName") String accountName, @FormDataParam("balance") int balance,
                         @FormDataParam("currency") String currency){

        try{
            PreparedStatement ps = main.db.prepareStatement("INSERT INTO Accounts (AccountID, AccountName, Balance, Currency) VALUES (?,?,?,?)");
            //Adds the account to the database
            out.println("/Accounts/new");
            ps.setString(1,null);//auto-increments the primary key
            fillColumn(accountName,balance,currency,ps,1);//Fills in the ps with the input data
            ps.executeUpdate();

            //Finds the auto-incremented ID for the new account
            PreparedStatement ps2 = main.db.prepareStatement("SELECT * FROM Accounts ORDER BY AccountID DESC LIMIT 1");
            ResultSet result = ps2.executeQuery();
            if (!result.next()) throw new Exception("Your SQL code doesn't work");

            //Sets the user as a manager on the account with the highest level of access.
            PreparedStatement ps3 = main.db.prepareStatement("INSERT INTO AccountManagers (ControlID, AccountID, ManagerID, AccessLevel) VALUES (?,?,?,?)");
            ps3.setString(1,null);
            ps3.setInt(2,result.getInt(1));
            ps3.setInt(3,userID);
            ps3.setInt(4,3);
            ps3.executeUpdate();

            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error when inputting user into database, error code\n" + e.getMessage());
            return "{\"error\": \"Unable to create new item, please see server console for more info.\"}";
        }
    }
    @POST
    @Path("edit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String update(@FormDataParam("accountID") int accountID, @FormDataParam("accountName") String accountName,
                         @FormDataParam("balance") int balance, @FormDataParam("currency") String currency){
        try{
            System.out.println("Accounts/edit id = " + accountID);

            PreparedStatement ps = main.db.prepareStatement("UPDATE Accounts SET AccountName = ?, Balance = ?, Currency = ? WHERE AccountID = ?");
            fillColumn(accountName,balance,currency,ps,0);
            ps.setInt(4, accountID);
            ps.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error updating account, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to update item, please see server console for more info.\"}";
        }
    }

    public static void delete(int accountID) {
        try {
            PreparedStatement ps = main.db.prepareStatement("DELETE FROM Accounts WHERE AccountID = ?");
            ps.setInt(1, accountID);
            ps.execute();

            out.println("Account number" + accountID + "was deleted successfully");

        } catch (Exception e) {
            out.println("Error deleting user, error message:\n" + e.getMessage());
        }
    }

    private static void fillColumn(String accountName, int balance, String currency, PreparedStatement ps, int column) throws SQLException {
        ps.setString(1+column, accountName);
        ps.setInt(2+column, balance);
        ps.setString(3+column, currency);
    }
}

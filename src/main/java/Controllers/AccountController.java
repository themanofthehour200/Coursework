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

@Path("Accounts/")//Sets the Path for all API calls in this class
public class AccountController {

    @GET
    @Path("viewAll/{id}")
    @Produces(MediaType.APPLICATION_JSON)//Jersey turns this into an HTTP request handler

    /*This method returns all the details of accounts that the user has access to view*/
    public String viewAll(@PathParam("id") int userID) {
        JSONArray list = new JSONArray();
        try{
        PreparedStatement ps = main.db.prepareStatement("SELECT A.AccountID, A.AccountName, A.Balance, A.Currency, AM.AccessLevel FROM Accounts A INNER JOIN AccountManagers AM ON A.AccountID = AM.AccountID AND AM.ManagerID = ?");
        ps.setInt(1,userID);

        ResultSet result = ps.executeQuery();

        while (result.next()) {//This will then display all of the details of account the user has access to in a JSON array
            JSONObject item = new JSONObject();
            item.put("AccountID", result.getInt(1));
            item.put("AccountName", result.getString(2));
            item.put("Balance", result.getInt(3));
            item.put("Currency", result.getString(4));
            item.put("AccessLevel",result.getInt(5));
            list.add(item);
        }
        return list.toString();

        } catch(Exception e) {
            System.out.println("Database error: " + e.getMessage());
            return "{\"error\": \"Unable to list items, please see server console for more info.\"}";
        }
    }

    @GET
    @Path("search/{id}")
    @Produces(MediaType.APPLICATION_JSON)

    //This returns a specific account's details
    public String search(@PathParam("id") Integer searchID){
        try {
            if (searchID == null) {
                throw new Exception("Thing's 'id' is missing in the HTTP request's URL.");
            }

            System.out.println("Accounts/search/" + searchID);
            JSONObject item = new JSONObject();

            PreparedStatement ps = main.db.prepareStatement("SELECT AccountID, AccountName, Balance, Currency FROM Accounts WHERE AccountID = ?");

            ps.setInt(1,searchID); //The user with the specific account ID is searched for

            ResultSet result = ps.executeQuery();

            if (result.next()) {
                item.put("AccountID", searchID);
                item.put("AccountName", result.getString(2));
                item.put("Balance", result.getInt(3));
                item.put("Currency", result.getString(4));
                return item.toString();
            } else{
                throw new Exception("Account doesn't exist");
            }
        }

        catch (Exception e){
            out.println("Error searching database 'Accounts', error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to get item, please see server console for more info.\"}";
        }
    }


    @POST
    @Path("accessCheck")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)//Jersey turns this into an HTTP request handler

    //This is the method for validating what the user's access level is on the account they are trying to access
    public String validate(@FormDataParam("userID") int userID, @FormDataParam("accountID") String accountID){
        System.out.println("/Accounts/accessCheck");
        try{

            //This will return the AccessLevel of the user and a few of their basic details
            PreparedStatement ps = main.db.prepareStatement("SELECT AccountManagers.AccessLevel, Users.UserID, Users.FirstName FROM AccountManagers " +
                    "INNER JOIN Users ON Users.UserID= AccountManagers.ManagerID AND Users.UserID = ? AND AccountManagers.AccountID = ?");

            ps.setInt(1,userID);
            ps.setString(2,accountID);
            ResultSet result = ps.executeQuery();
            JSONObject item = new JSONObject();

            //Only returns details if the user has been found as a manager, otherwise an error is given
            if(result.next()){
                item.put("AccessLevel",result.getInt(1));
                item.put("UserID",result.getInt(2));
                item.put("FirstName",result.getString(3));
                return item.toString();
            }else{//If no relationship between user and account is found
                throw new Exception("User has no access to account");
            }

        } catch (Exception e){
            System.out.println("Database error: " + e.getMessage());
            return "{\"error\": \"Unable to list items, please see server console for more info.\"}";
        }
    }


    @POST
    @Path("new")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)

    //This method creates a new account that the user is automatically made a manager of
    public String insert(@FormDataParam("userID") int userID, @FormDataParam("accountName") String accountName, @FormDataParam("balance") int balance,
                         @FormDataParam("currency") String currency){

        try{
            PreparedStatement ps = main.db.prepareStatement("INSERT INTO Accounts (AccountID, AccountName, Balance, Currency) VALUES (?,?,?,?)");
            //Adds the account to the database
            out.println("/Accounts/new");
            ps.setString(1,null);//auto-increments the primary key
            fillColumn(accountName,balance,currency,ps,1);//Fills in the ps with the input data
            ps.executeUpdate();

            //Finds the auto-incremented ID for the new account by seeing which accountID has been added last
            PreparedStatement ps2 = main.db.prepareStatement("SELECT * FROM Accounts ORDER BY AccountID DESC LIMIT 1");
            ResultSet result = ps2.executeQuery();

            /*This error is only thrown if the account that has just been created cannot be found
            The error code ascertains to the fact that the account that the user has just created
            will have to be found by an admin*/

            if (!result.next()) throw new Exception("Please email 87534@farnborough.ac.uk with code 'Viper'");

            //Sets the user as a manager on the account with the highest level of access.
            PreparedStatement ps3 = main.db.prepareStatement("INSERT INTO AccountManagers (ControlID, AccountID, ManagerID, AccessLevel) VALUES (?,?,?,?)");
            ps3.setString(1,null);
            ps3.setInt(2,result.getInt(1));
            ps3.setInt(3,userID);
            ps3.setInt(4,3);
            ps3.executeUpdate();

            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error when inputting account into database, error code\n" + e.getMessage());
            return "{\"error\": \"Unable to create new item, please see server console for more info.\"}";
        }
    }
    @POST
    @Path("edit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    //This method updates the details for a user's account
    public String update(@FormDataParam("accountID") int accountID, @FormDataParam("accountName") String accountName,
                         @FormDataParam("balance") int balance, @FormDataParam("currency") String currency){
        try{
            System.out.println("Accounts/edit id = " + accountID);

            PreparedStatement ps = main.db.prepareStatement("UPDATE Accounts SET AccountName = ?, Balance = ?, Currency = ? WHERE AccountID = ?");
            fillColumn(accountName,balance,currency,ps,0);//Fills in the values in the ps
            ps.setInt(4, accountID);
            ps.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error updating account, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to update item, please see server console for more info.\"}";
        }
    }

    @POST
    @Path("delete")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)

    //Deletes an existing account
    public String delete(@FormDataParam("accountID") Integer searchID){
        try{
            if (searchID == null) throw new Exception("One or more form data parameters are missing in the HTTP request.");

            out.println("Accounts/delete " + searchID);

            //This will CASCADE DELETE all other records associated with the account
            PreparedStatement ps = main.db.prepareStatement("DELETE FROM Accounts WHERE AccountID = ?");
            ps.setInt(1,searchID);
            ps.execute();

            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error deleting user, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to delete item, please see server console for more info.\"}";
        }
    }

    /*This method is used to efficiently fill the ps,
    as many API paths have nearly identical code within the class
    when filling in prepared statement*/
    private static void fillColumn(String accountName, int balance, String currency, PreparedStatement ps, int column) throws SQLException {
        //Column refers to which column number these variables should be filled in to, as it varies a little method by method
        ps.setString(1+column, accountName);
        ps.setInt(2+column, balance);
        ps.setString(3+column, currency);
    }
}

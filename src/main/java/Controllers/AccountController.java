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
    @POST
    @Path("viewAll")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)//Jersey turns this into an HTTP request handler

    /*This method returns all the details of accounts that the user has access to view*/
    public String viewAll(@FormDataParam("userID") int userID) {
        JSONArray list = new JSONArray();
        try{
        PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Accounts INNER JOIN AccountManagers AM ON Accounts.AccountID = AM.AccountID AND AM.ManagerID = ?");
        ps.setInt(1,userID);

        ResultSet result = ps.executeQuery();

        while (result.next()) {//This will then display all of the details of account the user has access to in a JSON array
            JSONObject item = new JSONObject();
            item.put("AccountID", result.getInt(1));
            item.put("AccountName", result.getString(2));
            item.put("Balance", result.getInt(3));
            item.put("Currency", result.getString(4));
            list.add(item);
        }
        return list.toString();

    } catch(Exception e) {
        System.out.println("Database error: " + e.getMessage());
        return "{\"error\": \"Unable to list items, please see server console for more info.\"}";
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
                item.put("accessLevel",result.getInt(1));
                item.put("userID",result.getInt(2));
                item.put("firstName",result.getString(3));
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

    private static void fillColumn(String accountName, int balance, String currency, PreparedStatement ps, int column) throws SQLException {
        ps.setString(1+column, accountName);
        ps.setInt(2+column, balance);
        ps.setString(3+column, currency);
    }
}

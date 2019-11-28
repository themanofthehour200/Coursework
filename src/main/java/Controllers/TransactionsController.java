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

@Path("Transactions/")
public class TransactionsController {

    @POST
    @Path("create")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)

    //This creates a new transaction
    public String insert(@FormDataParam("categoryID") int categoryID, @FormDataParam("date") String date, @FormDataParam("accountID") int accountID, @FormDataParam("balanceChange") int balanceChange,
                         @FormDataParam("description") String description, @FormDataParam("standingOrderID") int standingOrderID) {

        try {
            //Creates the ps for the SQL to create the transaction in the Transactions tables
            PreparedStatement ps = main.db.prepareStatement("INSERT INTO Transactions (TransactionID, AccountID, BalanceChange, CategoryID," +
                    " Description, Date, StandingOrderID) VALUES (?,?,?,?,?,?,?)"); //This adds the transaction to the transaction database

            //makes sure that a transaction with a value of zero isn't done, as this is a pointless transaction to make and may lead to errors
            if (balanceChange == 0) throw new Exception("Transaction cannot be for a value of 0");

            out.println("Transactions/create");

            ps.setString(1, null);//auto-increments the primary key

            //A function which fills the ps with the correct values
            fillColumn(accountID, balanceChange, categoryID, description, date, standingOrderID, ps, 1);

            ps.executeUpdate();

            /*Calls the method changingBalance. This will update the account balance
             to the correct amount once the transaction has been created. It returns a boolean
             value, which is indicative of whether the changes have been made successfully or not*/
            if (changingBalance(accountID, -(balanceChange))) return "{\"status\": \"OK\"}";
            else throw new Exception("Error when updating account balance");


        } catch (Exception e) {
            out.println("Error when creating transaction, error code\n" + e.getMessage());
            return "{\"error\": \"Unable to create new item, please see server console for more info.\"}";
        }
    }

    @GET
    @Path("view/{id}")
    @Produces(MediaType.APPLICATION_JSON)

    //This returns the details of all transactions done on all of a user's accounts
    public String view(@PathParam("id") Integer searchID) {
        try {
            if (searchID == null) {
                throw new Exception("Thing's 'id' is missing in the HTTP request's URL.");
            }

            System.out.println("Transactions/view/" + searchID);
            JSONArray list = new JSONArray();

            /*This SQL statement returns all of the details of the transactions,
            as well as some of the details of the user who is viewing the transactions */
            PreparedStatement ps = main.db.prepareStatement("SELECT Transactions.*, Accounts.AccountName FROM Transactions " +
                    "INNER JOIN Accounts ON Accounts.AccountID = Transactions.AccountID" +
                    " JOIN AccountManagers ON AccountManagers.AccountID = Transactions.AccountID " +
                    " JOIN Users ON Users.UserID = AccountManagers.ManagerID AND Users.UserID=?");

            ps.setInt(1, searchID); //The user with the specific account ID is searched for

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
                item.put("AccountName",result.getString(8));
                list.add(item);
            }
            return list.toString();

        } catch (Exception e) {
            out.println("Error searching database 'Transactions', error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to get item, please see server console for more info.\"}";

        }
    }

    @POST
    @Path("search")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)//Jersey turns this into an HTTP request handler
    //This method allows a user to search for a previous transaction based on criteria
    public String search(@FormDataParam("categoryID") int categoryID, @FormDataParam("date") String date, @FormDataParam("accountID") int accountID) {

        System.out.println("Transactions/search");
        JSONArray list = new JSONArray();

        try {
            //Select it for the account and for any details that the user has entered
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Transactions WHERE AccountID = ? AND Date = ? OR CategoryID = ?");
            ps.setInt(1, accountID);
            ps.setString(2, date);
            ps.setInt(3, categoryID);
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
            else
                throw new Exception("No transactions with search criteria found"); //if none have been found then give an error

        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
            return "{\"error\": \"Unable to list items, please see server console for more info.\"}";
        }
    }

    @POST
    @Path("edit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    //This method edits a previous tranaction and updates the account's balance to reflect this
    public String update(@FormDataParam("transactionID") int transactionID, @FormDataParam("categoryID") int categoryID, @FormDataParam("date") String date,
                         @FormDataParam("accountID") int accountID, @FormDataParam("balanceChange") int balanceChange,
                         @FormDataParam("description") String description, @FormDataParam("standingOrderID") int standingOrderID) {
        try {
            System.out.println("Transactions/edit id = " + transactionID);
            //Stops any null balance transactions
            if (balanceChange == 0) throw new Exception("Transaction cannot be for a value of 0");

            PreparedStatement ps = main.db.prepareStatement("SELECT BalanceChange FROM Transactions WHERE TransactionID = ?");
            ps.setInt(1, transactionID);
            ResultSet result = ps.executeQuery();
            //This value is the amount by which the account balance will change due to the transaction amount changing
            int accountChange = result.getInt(1) - balanceChange;

            PreparedStatement ps2 = main.db.prepareStatement("UPDATE Transactions SET AccountID = ?, BalanceChange = ?, CategoryID= ?, Description = ?, " +
                    "Date = ?,  StandingOrderID = ? WHERE TransactionID = ?");

            fillColumn(accountID, balanceChange, categoryID, description, date, standingOrderID, ps2, 0);
            ps2.setInt(7, transactionID);
            ps2.executeUpdate();


            /*Calls the method changingBalance. This will update the account balance
             to the correct amount once the transaction has been created. It returns a boolean
             value, which is indicative of whether the changes have been made successfully or not*/
            if (changingBalance(accountID, accountChange)) return "{\"status\": \"OK\"}";
            else throw new Exception("Error when updating account balance");

        } catch (Exception e) {
            out.println("Error updating account, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to update item, please see server console for more info.\"}";
        }
    }

    //Deletes an existing transaction
    @POST
    @Path("delete")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String delete(@FormDataParam("transactionID") Integer searchID){
        try{
            /*searchID is stored as an Integer instead of an int to allow for this if statement
            as 'int' values cannot be null*/

            if (searchID == null) throw new Exception("One or more form data parameters are missing in the HTTP request.");

            out.println("Transactions/delete id = " + searchID);

            PreparedStatement ps = main.db.prepareStatement("SELECT AccountID, BalanceChange FROM Transactions WHERE TransactionID = ?");
            ps.setInt(1, searchID);
            ResultSet result = ps.executeQuery();

            //The accountID of the account that needs its balance updated
            int accountID = result.getInt(1);
            //This value is the amount by which the account balance will change due to the transaction amount changing
            int accountChange = result.getInt(2);

            PreparedStatement ps2 = main.db.prepareStatement("DELETE FROM Transactions WHERE TransactionID = ?");
            ps2.setInt(1,searchID);
            ps2.execute();

            /*Calls the method changingBalance. This will update the account balance
             to the correct amount once the transaction has been created. It returns a boolean
             value, which is indicative of whether the changes have been made successfully or not*/

            if (changingBalance(accountID, accountChange)) return "{\"status\": \"OK\"}";
            else throw new Exception("Error when updating account balance");

        } catch (Exception e){
            out.println("Error deleting user, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to delete item, please see server console for more info.\"}";
        }
    }

    /*This method is used to efficiently fill the ps,
    as many API paths have nearly identical code within the class
    when filling in prepared statement*/
    private static void fillColumn(int accountID, int balanceChange, int categoryID, String description, String date, int standingOrderID, PreparedStatement ps, int column) throws SQLException {

        ps.setInt(1 + column, accountID);
        ps.setInt(2 + column, balanceChange);
        ps.setInt(3 + column, categoryID);
        ps.setString(4 + column, description);
        ps.setString(5 + column, date);
        ps.setInt(6 + column, standingOrderID);
    }

    /*This function updates the balance of an account to reflect a transaction being created or edited
    * This is in a separate method to reduce duplicate code, as both new transactions and
    * editing previous transactions use this code*/
    private static boolean changingBalance(int accountID, int accountChange) {
        try {
            //This gets the account balance for the account the transaction is relating to
            PreparedStatement ps = main.db.prepareStatement("SELECT Balance FROM Accounts WHERE AccountID = ?");

            ps.setInt(1, accountID); //The user with the specific account ID is searched for
            ResultSet result = ps.executeQuery();

            //This updates the account balance to reflect the transaction

            PreparedStatement ps2 = main.db.prepareStatement("UPDATE Accounts SET Balance = ? WHERE AccountID = ?");
            ps2.setInt(1, result.getInt(1) + accountChange);
            ps2.setInt(2, accountID);
            ps2.executeUpdate();

            return true;
        } catch (Exception e) {
            return false;
        }

    }
}

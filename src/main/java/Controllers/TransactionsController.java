package Controllers;

import Server.main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class TransactionsController{

    //This is the method for selecting all rows in the table of Users
    //This method is mainly just used for testing purposes, as this is easier than manually having to check the Accounts table after each applicable test
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
package Controllers;

import Server.main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

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

    public static void insert(String accountName, int balance, String currency) {

        try {
            PreparedStatement ps = main.db.prepareStatement("INSERT INTO Accounts (AccountID, AccountName, Balance, Currency) VALUES (?,?,?,?)");
            ps.setString(1, null);//As it auto-increments
            fillColumn(accountName,balance,currency,ps,1);
            ps.executeUpdate();

        } catch (Exception e) {
            out.println("Error when inputting account into database, please email our dedicated support team " +
                    "at 87534@farnborough.ac.uk with error code:\n" + e.getMessage());
        }
    }


    public static void update(int accountID, String accountName, int balance, String currency) {
        try {
            PreparedStatement ps = main.db.prepareStatement("UPDATE Accounts SET AccountName = ?, Balance = ?, Currency = ? WHERE AccountID = ?");
            fillColumn(accountName,balance,currency,ps,0);
            ps.setInt(4, accountID);
            ps.executeUpdate();

        } catch (Exception e) {
            out.println("Error updating user, error message:\n" + e.getMessage());
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

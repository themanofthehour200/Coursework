import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class BudgetsController{

    //This is the method for selecting all rows in the table of Users
    //This method is mainly just used for testing purposes, as this is easier than manually having to check the Accounts table after each applicable test
    public static List selectAll(){
        try{
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Budgets");
            ResultSet result = ps.executeQuery();

            int count = 0;
            List<List<String>> output = new ArrayList<List<String>>(); //This is a List of ArrayLists. This is what is returned.
            //An ArrayList is used instead of an array as it is mutatable and we don't know how many rows there are in the table

            while(result.next()){
                output.add(new ArrayList<String>());            //A new arraylist is created within the overall output List
                output.get(count).add(Integer.toString(result.getInt(1)));      //The value is added in to the current ArrayList within output
                output.get(count).add(Integer.toString(result.getInt(2)));
                output.get(count).add(Integer.toString(result.getInt(3)));
                output.get(count).add(Integer.toString(result.getInt(4)));
                output.get(count).add(Integer.toString(result.getInt(5)));
                output.get(count).add(Integer.toString(result.getInt(6)));
                output.get(count).add(result.getString(7));
                out.println(output.get(count)); //To be removed once testing phase one is done
                count++;
            }
            return output;

        } catch (Exception e){
            out.println("Error reading database 'Budgets', error message:\n" + e.getMessage());
            return null;
        }
    }

    //This returns a specific accounts details, allowing the user to check their balance etc.
    public static List search(int searchID){
        try {
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Budgets WHERE BudgetID = ?");
            ps.setInt(1,searchID); //The user with the specific account ID is searched for
            ResultSet result = ps.executeQuery();

            ArrayList<String> output = new ArrayList<String>(1);
            output.add(Integer.toString(result.getInt(1)));      //The value is added in to the current ArrayList within output
            output.add(Integer.toString(result.getInt(2)));
            output.add(Integer.toString(result.getInt(3)));
            output.add(result.getString(result.getInt(4)));
            output.add(result.getString(result.getInt(5)));
            output.add(result.getString(result.getInt(6)));
            output.add(result.getString(7));
            out.println(output);
            return output;

        }
        catch (Exception e){
            out.println("Error searching database 'Budgets', error message:\n" + e.getMessage());
            return null;
        }

    }

    public static void insert(int budgetID, int userID, int categoryID, int amount, int balance, int duration, String dateStarted){

        try{
            PreparedStatement ps = main.db.prepareStatement("INSERT INTO Budgets (BudgetID, UserID, CategoryID, Amount, Balance, Duration, DateStarted) VALUES (?,?,?,?,?,?,?)");

            fillColumn(budgetID, userID, categoryID, amount, balance, duration, dateStarted, ps);

            ps.executeUpdate();

        } catch (Exception e){
            out.println("Error when inputting transaction into database, error code\n" + e.getMessage());
        }
    }

    public static void delete(int searchID){
        try{
            PreparedStatement ps = main.db.prepareStatement("DELETE FROM Budgets WHERE BudgetID = ?");
            ps.setInt(1,searchID);
            ps.execute();

            out.println("Budget number " + searchID + " was deleted successfully");

        } catch (Exception e){
            out.println("Error deleting budget, error message:\n" + e.getMessage());
        }
    }

    public static void update(int budgetID, int userID, int categoryID, int amount, int balance, int duration, String dateStarted){
        try{
            PreparedStatement ps = main.db.prepareStatement("UPDATE Budgets SET BudgetID = ?, UserID = ?, CategoryID = ?, Amount = ?, Balance = ?, Duration = ?, DateStarted = ?WHERE BudgetID = ?");
            fillColumn(budgetID, userID, categoryID, amount, balance, duration, dateStarted, ps);
            ps.setInt(8, budgetID);
            ps.executeUpdate();

        } catch (Exception e){
            out.println("Error updating order, error message:\n" + e.getMessage());
        }
    }

    /* removes the duplicate code of the data entry into the SQL statement for update() and add(), as there code was very similar */
    private static void fillColumn(int budgetID, int userID, int categoryID, int amount, int balance, int duration, String dateStarted, PreparedStatement ps) throws SQLException {
        ps.setInt(1,budgetID);
        ps.setInt(2, userID);
        ps.setInt(3, categoryID);
        ps.setInt(4,amount);
        ps.setInt(5,balance);
        ps.setInt(6,duration);
        ps.setString(7,dateStarted);
    }

}

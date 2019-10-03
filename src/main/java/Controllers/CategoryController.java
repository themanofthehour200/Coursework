package Controllers;

import Server.main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class CategoryController{

    //This is the method for selecting all rows in the table of Users
    //This method is mainly just used for testing purposes, as this is easier than manually having to check the Accounts table after each applicable test
    public static List selectAll(){
        try{
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Categories");
            ResultSet result = ps.executeQuery();

            int count = 0;
            List<List<String>> output = new ArrayList<List<String>>(); //This is a List of ArrayLists. This is what is returned.
            //An ArrayList is used instead of an array as it is mutatable and we don't know how many rows there are in the table

            while(result.next()){
                output.add(new ArrayList<String>());            //A new arraylist is created within the overall output List
                output.get(count).add(Integer.toString(result.getInt(1)));      //The value is added in to the current ArrayList within output
                output.get(count).add(result.getString(2));
                output.get(count).add(Integer.toString(result.getInt(3)));
                out.println(output.get(count)); //To be removed once testing phase one is done
                count++;
            }
            return output;

        } catch (Exception e){
            out.println("Error reading database 'Category', error message:\n" + e.getMessage());
            return null;
        }
    }

    //This returns a specific accounts details, allowing the user to check their balance etc.
    public static List search(int searchID){
        try {
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Categories WHERE CategoryID = ?");
            ps.setInt(1,searchID); //The user with the specific account ID is searched for
            ResultSet result = ps.executeQuery();

            ArrayList<String> output = new ArrayList<String>(1);
            output.add(Integer.toString(result.getInt(1)));      //The value is added in to the current ArrayList within output
            output.add(result.getString(2));
            output.add(Integer.toString(result.getInt(3)));

            out.println(output);
            return output;

        }
        catch (Exception e){
            out.println("Error searching database 'Category', error message:\n" + e.getMessage());
            return null;
        }

    }

    public static void insert(String categoryName, int accessID){

        try{
            PreparedStatement ps = main.db.prepareStatement("INSERT INTO Categories (CategoryID, CategoryName, AccessID) VALUES (?,?,?)");

            ps.setString(1,null);
            ps.setString(2, categoryName);
            ps.setInt(3, accessID);

            ps.executeUpdate();

        } catch (Exception e){
            out.println("Error when inputting category into database, error code\n" + e.getMessage());
        }
    }

    public static void delete(int searchID){
        try{
            PreparedStatement ps = main.db.prepareStatement("DELETE FROM Categories WHERE CategoryID = ?");
            ps.setInt(1,searchID);
            ps.execute();

            out.println("Category number " + searchID + " was deleted successfully");

        } catch (Exception e){
            out.println("Error deleting category, error message:\n" + e.getMessage());
        }
    }

    public static void update(int categoryID, String categoryName, int accessID){
        try{
            PreparedStatement ps = main.db.prepareStatement("UPDATE Categories SET CategoryName = ?, AccessID = ?  WHERE CategoryID = ?");

            ps.setString(1,categoryName);
            ps.setInt(2, accessID);
            ps.setInt(3,categoryID);
            ps.executeUpdate();

        } catch (Exception e){
            out.println("Error updating category, error message:\n" + e.getMessage());
        }
    }

}

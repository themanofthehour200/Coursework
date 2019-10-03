package Controllers;

import Server.main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

@Path("Users/")
public class UserController {

    //This is the method for selecting all rows in the table of Users
    //This method is mainly just used for testing purposes, as this is easier than manually having to check the Accounts table after each applicable test

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)//Jersey turns this into an HTTP request handler
    public String selectAll(){
        System.out.println("Users/list");
        JSONArray list = new JSONArray();
        try{
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Users");
            ResultSet result = ps.executeQuery();

            while(result.next()){
                JSONObject item = new JSONObject();
                item.put("UserId", result.getInt(1));
                item.put("FirstName", result.getString(2));
                item.put("Surname", result.getString(3));
                item.put("DateOfBirth", result.getString(4));
                item.put("Email", result.getString(5));
                item.put("PhoneNumber", result.getString(6));
                item.put("Password", result.getString(7));
                list.add(item);

            }
            return list.toString();

        } catch (Exception e){
            System.out.println("Database error: " + e.getMessage());
            return "{\"error\": \"Unable to list items, please see server console for more info.\"}";
        }
    }

    //This returns a specific accounts details, allowing the user to check their balance etc.
    public static List search(int searchID, String email, String password){
        try {
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Users WHERE UserID = ? OR (Email = ? AND Password = ?)");

            ps.setInt(1,searchID); //The user with the specific account ID is searched for
            ps.setString(2,email);
            ps.setString(3,password);

            ResultSet result = ps.executeQuery();

            ArrayList<String> output = new ArrayList<String>(1);
            output.add(Integer.toString(result.getInt(1)));      //The value is added in to the current ArrayList within output
            output.add(result.getString(2));
            output.add(result.getString(3));
            output.add(result.getString(4));
            output.add(result.getString(5));
            output.add(result.getString(6));
            output.add(result.getString(7));

            out.println(output);
            return output;

        }
        catch (Exception e){
            out.println("Error searching database 'Users', error message:\n" + e.getMessage());
            return null;
        }
    }

    public static void insert(String firstName,String surname, String dateOfBirth,String email, String phoneNumber, String password){

        try{
            PreparedStatement ps = main.db.prepareStatement("INSERT INTO Users (UserID, FirstName, Surname, DateOfBirth, Email, PhoneNumber, Password) VALUES (?,?,?,?,?,?,?)");

            ps.setString(1,null);//auto-increments the primary key
            fillColumn(firstName, surname, dateOfBirth, email, phoneNumber, password, ps, 1);
            ps.executeUpdate();
            out.println("User added successfully");

        } catch (Exception e){
            out.println("Error when inputting user into database, error code\n" + e.getMessage());
        }
    }

    public static void delete(int searchID){
        try{
            PreparedStatement ps = main.db.prepareStatement("DELETE FROM Users WHERE UserID = ?");
            ps.setInt(1,searchID);
            ps.execute();

            out.println("Account number" + searchID + "was deleted successfully");

        } catch (Exception e){
            out.println("Error deleting user, error message:\n" + e.getMessage());
        }
    }

    public static void update(int userID, String firstName,String surname, String dateOfBirth,String email, String phoneNumber, String password){
        try{
            PreparedStatement ps = main.db.prepareStatement("UPDATE Users SET FirstName = ?, Surname = ?, DateOfBirth = ?, Email = ?, PhoneNumber = ?, Password = ? WHERE UserID = ?");
            fillColumn(firstName, surname, dateOfBirth, email, phoneNumber, password, ps,0);
            ps.setInt(7,userID);
            ps.executeUpdate();

        } catch (Exception e){
            out.println("Error updating user, error message:\n" + e.getMessage());
        }
    }

    /* removes the duplicate code of the data entry into the SQL statement for update() and add(), as there code was very similar */
    private static void fillColumn(String firstName,String surname, String dateOfBirth, String email, String phoneNumber, String password, PreparedStatement ps, int column) throws SQLException {
        ps.setString(1+column,firstName);//column is needed as the column numbers are slightly different between
        ps.setString(2+column,surname);//the insert method and the update method
        ps.setString(3+column,dateOfBirth);
        ps.setString(4+column,email);
        ps.setString(5+column,phoneNumber);
        ps.setString(6+column,password);
    }

}

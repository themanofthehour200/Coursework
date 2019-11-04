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

@Path("Users/")
public class UserController{

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

    @GET
    @Path("search/{id}")
    @Produces(MediaType.APPLICATION_JSON)

    //This returns a specific user's details
    public String search(@PathParam("id") Integer searchID){
        try {
            if (searchID == null) {
                throw new Exception("Thing's 'id' is missing in the HTTP request's URL.");
            }

            System.out.println("Users/get/" + searchID);
            JSONObject item = new JSONObject();

            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Users WHERE UserID = ?");

            ps.setInt(1,searchID); //The user with the specific account ID is searched for

            ResultSet result = ps.executeQuery();

            if (result.next()) {
                item.put("UserID", searchID);
                item.put("Firstname", result.getString(1));
                item.put("Surname", result.getString(2));
                item.put("DateOfBirth", result.getString(3));
                item.put("Email", result.getString(4));
                item.put("PhoneNumber", result.getString(5));
                item.put("Password", result.getString(6));
                return item.toString();
            } else{
                throw new Exception("User doesn't exist");
            }

        }
        catch (Exception e){
            out.println("Error searching database 'Users', error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to get item, please see server console for more info.\"}";

        }
    }

    @POST
    @Path("new")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)

    public String insert(@FormDataParam("firstName") String firstName, @FormDataParam("surname") String surname, @FormDataParam("dateOfBirth") String dateOfBirth,
                              @FormDataParam("email") String email, @FormDataParam("phoneNumber") String phoneNumber, @FormDataParam("password") String password){

        try{
            PreparedStatement psCheck = main.db.prepareStatement("SELECT UserID FROM Users WHERE Email = ?");
            psCheck.setString(1,email); //This validates that a user is being duplicated, as the email has to be unique to each user
            ResultSet resultCheck = psCheck.executeQuery();
            if (resultCheck.next()) {
                throw new Exception("User already exists");
            }

            /* This uses the varValid() methods in the 'main' class to ascertain if the inputs are in their valid formats or not. */
            if (!main.nameValid(firstName) || !main.nameValid(surname) || dateOfBirth == null || !main.emailValid(email) || !main.passwordValid(password)) {
                throw new Exception("One or more of the form parameters are missing or in the wrong data format");
            }

            PreparedStatement ps = main.db.prepareStatement("INSERT INTO Users (UserID, FirstName, Surname, DateOfBirth, Email, PhoneNumber, Password) VALUES (?,?,?,?,?,?,?)");

            out.println("/Users/new");
            ps.setString(1,null);//auto-increments the primary key
            fillColumn(firstName, surname, dateOfBirth, email, phoneNumber, password, ps, 1);
            ps.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error when inputting user into database, error code\n" + e.getMessage());
            return "{\"error\": \"Unable to create new item, please see server console for more info.\"}";
        }
    }

    @POST
    @Path("delete")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String delete(@FormDataParam("id") Integer searchID){
        try{
            if (searchID == null) throw new Exception("One or more form data parameters are missing in the HTTP request.");

            out.println("Users/delete" + searchID);

            PreparedStatement ps = main.db.prepareStatement("DELETE FROM Users WHERE UserID = ?");
            ps.setInt(1,searchID);
            ps.execute();

            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error deleting user, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to delete item, please see server console for more info.\"}";
        }
    }

    @POST
    @Path("update")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String update(@FormDataParam("UserID") int userID, @FormDataParam("firstName") String firstName, @FormDataParam("surname") String surname, @FormDataParam("dateOfBirth") String dateOfBirth,
                         @FormDataParam("email") String email, @FormDataParam("phoneNumber") String phoneNumber, @FormDataParam("password") String password){
        try{
            /* This uses the varValid() methods in the 'main' class to ascertain if the inputs are in their valid formats or not. */
            if (!main.nameValid(firstName) || !main.nameValid(surname) || dateOfBirth == null || !main.emailValid(email) || !main.passwordValid(password)) {
                throw new Exception("One or more of the form parameters are missing or in the wrong data format");
            }

            System.out.println("Users/update id=" + userID);

            PreparedStatement ps = main.db.prepareStatement("UPDATE Users SET FirstName = ?, Surname = ?, DateOfBirth = ?, Email = ?, PhoneNumber = ?, Password = ? WHERE UserID = ?");
            fillColumn(firstName, surname, dateOfBirth, email, phoneNumber, password, ps,0);
            ps.setInt(7,userID);
            ps.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error updating user, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to update item, please see server console for more info.\"}";
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

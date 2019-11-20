package Controllers;

import Server.main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static java.lang.System.out;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("Users/")
public class UserController {

    @POST
    @Path("validate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)//Jersey turns this into an HTTP request handler
    //This is the method for validating that the client's token is valid
    public String validate(@FormDataParam("userID") int userID, @FormDataParam("token") String token){
        System.out.println("/Users/validate");


        try{
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Users WHERE UserID = ? AND Token = ?");
            ps.setInt(1,userID);
            ps.setString(2,token);
            ResultSet result = ps.executeQuery();
            JSONObject item = new JSONObject();
            item.put("Valid", result.next());//If any results are brought back is whether the token is correct or not
            return item.toString();

        } catch (Exception e){
            System.out.println("Database error: " + e.getMessage());
            return "{\"error\": \"Unable to list items, please see server console for more info.\"}";
        }
    }

//This is the method for logging in
    @POST
    @Path("login")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String login(@FormDataParam("email") String email, @FormDataParam("password") String password){
        System.out.println("Users/login");
        JSONObject item = new JSONObject();

        try{
            PreparedStatement ps = main.db.prepareStatement("SELECT UserID FROM Users WHERE Email = ? AND Password = ?");
            ps.setString(1,email);
            ps.setString(2,password);
            ResultSet result = ps.executeQuery();

            if (result.next()) { //If user has correct log-in details
                item.put("UserID", result.getInt(1));
                String token = UUID.randomUUID().toString();//generates log-in token to be made into a cookie by the client
                item.put("Token",token);

                out.println("Logged in successfully");

                PreparedStatement ps2 = main.db.prepareStatement("UPDATE Users SET Token = ? WHERE UserID = ?"); //Updating token in database
                ps2.setString(1,token);
                ps2.setInt(2,result.getInt(1));
                ps2.executeUpdate();

                return item.toString();
            } else{
                throw new Exception("Invalid password or email");
            }

        } catch (Exception e){
            System.out.println("Database error: " + e.getMessage());
            return "{\"error\": \"Unable to login, please see server console for more info.\"}";
        }
    }

/*    This is the method for selecting all rows in the table of Users
    This method is mainly just used for testing purposes, as this is easier than
    manually having to check the Accounts table after each applicable test*/
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
                item.put("Token",result.getString(8));
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
                item.put("Firstname", result.getString(2));
                item.put("Surname", result.getString(3));
                item.put("DateOfBirth", result.getString(4));
                item.put("Email", result.getString(5));
                item.put("PhoneNumber", result.getString(6));
                item.put("Password", result.getString(7));
                item.put("Token", result.getString(8));
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

    //This method is for new users who are being added to te database
    public String insert(@FormDataParam("firstName") String firstName, @FormDataParam("surname") String surname, @FormDataParam("dateOfBirth") String dateOfBirth,
                              @FormDataParam("email") String email, @FormDataParam("phoneNumber") String phoneNumber, @FormDataParam("password") String password){

        try{
            out.println("/Users/new");

            PreparedStatement ps = main.db.prepareStatement("INSERT INTO Users (UserID, FirstName, Surname, DateOfBirth, Email, PhoneNumber, Password) VALUES (?,?,?,?,?,?,?)");

            /* This uses the varValid() methods in the 'main' class to ascertain if the inputs are in their valid formats or not. */
            if (!main.nameValid(firstName) || !main.nameValid(surname) || dateOfBirth == null || !main.emailValid(email) || !main.passwordValid(password)) {
                throw new Exception("One or more of the form parameters are missing or in the wrong data format");
            }

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

    //Deletes an existing user
    public String delete(@FormDataParam("userID") Integer searchID){
        try{
            if (searchID == null) throw new Exception("One or more form data parameters are missing in the HTTP request.");

            out.println("Users/delete " + searchID);

            //This will CASCADE DELETE all other records associated with the user
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
    @Path("edit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    //This method is used to update the details of an existing user
    public String update(@FormDataParam("userID") int userID, @FormDataParam("firstName") String firstName, @FormDataParam("surname") String surname, @FormDataParam("dateOfBirth") String dateOfBirth,
                         @FormDataParam("email") String email, @FormDataParam("phoneNumber") String phoneNumber, @FormDataParam("password") String password){
        try{
            /* This uses the varValid() methods in the 'main' class to ascertain if the inputs are in their valid formats or not. */
            if (!main.nameValid(firstName) || !main.nameValid(surname) || dateOfBirth == null || !main.emailValid(email) || !main.passwordValid(password)) {
                throw new Exception("One or more of the form parameters are missing or in the wrong data format");
            }

            System.out.println("Users/edit id = " + userID);

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

    /*This method is used to efficiently fill the ps,
    as many API paths have nearly identical code within the class
    when filling in prepared statement*/
    private static void fillColumn(String firstName,String surname, String dateOfBirth, String email, String phoneNumber, String password, PreparedStatement ps, int column) throws SQLException {
        ps.setString(1+column,firstName);//column is needed as the column numbers are slightly different between
        ps.setString(2+column,surname);//the insert method and the update method
        ps.setString(3+column,dateOfBirth);
        ps.setString(4+column,email);
        ps.setString(5+column,phoneNumber);
        ps.setString(6+column,password);
    }

}

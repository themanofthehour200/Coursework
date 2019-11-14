package Controllers;

import Server.main;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

@Path("Categories/")
public class CategoryController{

    //This method returns all categories available to the user
    @GET
    @Path("list/{userID}")
    @Produces(MediaType.APPLICATION_JSON)

    //This returns a specific user's details
    public String search(@PathParam("userID") Integer searchID){
        System.out.println("Categories/list/" + searchID);

        try{
            if (searchID == null) throw new Exception("No user exists");
            JSONArray list = new JSONArray();

            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Categories WHERE AccessID = 0 OR AccessID = ?");
            ps.setInt(1,searchID);
            ResultSet result = ps.executeQuery();

            while(result.next()){
                JSONObject item = new JSONObject();
                item.put("CategoryID", result.getInt(1));
                item.put("CategoryName", result.getString(2));
                item.put("AccessID", result.getInt(3));
                list.add(item);
            }
            return list.toString();

        } catch (Exception e){
            System.out.println("Database error: " + e.getMessage());
            return "{\"error\": \"Unable to list items, please see server console for more info.\"}";
        }
    }


    @POST
    @Path("new")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)

    public String insert(@FormDataParam("categoryName") String categoryName, @FormDataParam("accessID") int accessID){

        try{
            out.println("/Categories/new");

            //This is to ensure that no user has access to multiple categories with the same name
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Categories WHERE CategoryName = ? AND AccessID = ? OR AccessID = 0");
            ps.setString(1,categoryName);
            ps.setInt(2,accessID);
            ResultSet result = ps.executeQuery();
            if(result.next()) throw new Exception("Category already exists");

            //Preparing to insert the new category into the database
            PreparedStatement ps2 = main.db.prepareStatement("INSERT INTO Categories (CategoryID, CategoryName, AccessID) VALUES (?,?,?)");


            ps2.setString(1,null);//auto-increments the primary key
            ps2.setString(2,categoryName);
            ps2.setInt(3,accessID);
            ps2.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error when inputting category into database, error code\n" + e.getMessage());
            return "{\"error\": \"Unable to create new item, please see server console for more info.\"}";
        }
    }

    @POST
    @Path("delete")
    /*The reason that this uses form data instead of having the data be sent via the API path in the
      path name is that the form is more secure*/
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String delete(@FormDataParam("categoryID") Integer searchID){
        try{
            //This throws when there has been no categoryID entered
            if (searchID == null) throw new Exception("One or more form data parameters are missing in the HTTP request.");

            //This makes sure that the client can't change any of the default categories
            if(searchID <= 10 && searchID >= 0) throw new Exception("Can't delete default category");

            out.println("Categories/delete id = " + searchID);

            PreparedStatement ps = main.db.prepareStatement("DELETE FROM Categories WHERE CategoryID = ?");
            ps.setInt(1,searchID);
            ps.execute();

            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error deleting category, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to delete item, please see server console for more info.\"}";
        }
    }

}

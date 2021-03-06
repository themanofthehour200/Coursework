package Controllers;

import Server.main;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static java.lang.System.out;

@Path("Categories/")
public class CategoryController{

    //This method returns all categories available to the user
    @GET
    @Path("list/{userID}")
    @Produces(MediaType.APPLICATION_JSON)

    /*This returns all categories that are available to the user,
    including user-made categories and the default 10 categories*/

    public String list(@PathParam("userID") Integer searchID){
        System.out.println("Categories/list/" + searchID);

        try{
            if (searchID == null) throw new Exception("No user exists");
            JSONArray list = new JSONArray();

            //Categories with AccessID = 0 are the default categories and so are also returned
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

    //This returns values for a specific category
    @GET
    @Path("search/{categoryID}")
    @Produces(MediaType.APPLICATION_JSON)
    public String search(@PathParam("categoryID") Integer searchID){
        System.out.println("Categories/search/" + searchID);

        try{
            JSONObject item = new JSONObject();

            //Categories with AccessID = 0 are the default categories and so are also returned
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Categories WHERE CategoryID = ?");
            ps.setInt(1,searchID);
            ResultSet result = ps.executeQuery();

            if(result.next()){
                item.put("CategoryID", result.getInt(1));
                item.put("CategoryName", result.getString(2));
                item.put("AccessID", result.getInt(3));
                return item.toString();
            }else{
                throw new Exception("Category not found");
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

    public String insert(@FormDataParam("categoryName") String categoryName, @FormDataParam("accessID") int accessID){

        try{
            out.println("/Categories/new");

            //This is to ensure that no user has access to multiple categories with the same name
            PreparedStatement ps = main.db.prepareStatement("SELECT CategoryID FROM Categories WHERE CategoryName = ? AND AccessID = ? OR CategoryName = ? AND AccessID = 0");
            ps.setString(1,categoryName);
            ps.setInt(2,accessID);
            ps.setString(3,categoryName);

            ResultSet result = ps.executeQuery();
            if(result.next()){
                out.println("going to a return statement");
                return"{\"error\": \"category already exists\"}";
            }

            //Preparing to insert the new category into the database
            out.println("inserting category");
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
    @Path("edit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    //This method updates an already created budget
    public String update(@FormDataParam("categoryID") int categoryID, @FormDataParam("categoryName") String categoryName){
        try{

            System.out.println("Categories/edit id = " + categoryID);


            PreparedStatement ps = main.db.prepareStatement("UPDATE Categories SET CategoryName = ? WHERE CategoryID = ?");

            ps.setString(1,categoryName);
            ps.setInt(2,categoryID);

            ps.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error updating category, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to update item, please see server console for more info.\"}";
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

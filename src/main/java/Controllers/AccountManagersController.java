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
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

@Path("AccountManagers/")
public class AccountManagersController{

    //This method returns all the managers for an account
    @GET
    @Path("list/{accountID}")
    @Produces(MediaType.APPLICATION_JSON)

    public String search(@PathParam("accountID") Integer searchID){
        System.out.println("AccountManagers/list/" + searchID);

        try{
            if (searchID == null) throw new Exception("No account exists");
            JSONArray list = new JSONArray();

            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM AccountManagers WHERE AccountID = ?");
            ps.setInt(1,searchID);
            ResultSet result = ps.executeQuery();

            while(result.next()){
                JSONObject item = new JSONObject();
                item.put("ControlID", result.getInt(1));
                item.put("AccountID", result.getInt(2));
                item.put("ManagerID", result.getInt(3));
                item.put("AccessLevel", result.getInt(4));
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

    public String insert(@FormDataParam("accountID") int accountID, @FormDataParam("managerID") int managerID, @FormDataParam("AccessLevel") int accessLevel){

        try{
            out.println("/AccountManagers/new");

            //This is to ensure that no one account has the same user being a manager on it twice
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM AccountManagers WHERE AccountID = ? AND ManagerID = ?");
            ps.setInt(1,accountID);
            ps.setInt(2,managerID);
            ResultSet result = ps.executeQuery();
            if(result.next()) throw new Exception("User is already a manager");

            //Preparing to insert the new manager into the database
            PreparedStatement ps2 = main.db.prepareStatement("INSERT INTO AccountManagers (ControlID, AccountID, ManagerID, AccessLevel) VALUES (?,?,?,?)");


            ps2.setString(1,null);//auto-increments the primary key
            fillColumn(accountID,managerID,accessLevel,ps2,1);
            ps2.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error when inputting manager into database, error code\n" + e.getMessage());
            return "{\"error\": \"Unable to create new item, please see server console for more info.\"}";
        }
    }


    @POST
    @Path("edit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String update(@FormDataParam("controlID") int controlID, @FormDataParam("accountID") int accountID, @FormDataParam("managerID") int managerID, @FormDataParam("AccessLevel") int accessLevel){
        try{

            System.out.println("AccountManagers/edit id = " + controlID);

            PreparedStatement ps = main.db.prepareStatement("UPDATE AccountManagers SET AccountID = ?, ManagerID = ?, AccessLevel = ? WHERE ControlID = ?");
            fillColumn(accountID,managerID,accessLevel, ps,0);
            ps.setInt(4,controlID);
            ps.executeUpdate();
            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error updating Account Manager, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to update item, please see server console for more info.\"}";
        }
    }

    @POST
    @Path("delete")
    /*The reason that this uses form data instead of having the data be sent via the API path in the
      path name is that the form is more secure*/
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String delete(@FormDataParam("controlID") Integer searchID){
        try{
            //This throws when there has been no controlID entered
            if (searchID == null) throw new Exception("One or more form data parameters are missing in the HTTP request.");

            out.println("AccountManagers/delete id = " + searchID);

            PreparedStatement ps = main.db.prepareStatement("DELETE FROM AccountManagers WHERE ControlID = ?");
            ps.setInt(1,searchID);
            ps.execute();

            return "{\"status\": \"OK\"}";

        } catch (Exception e){
            out.println("Error deleting Manager, error message:\n" + e.getMessage());
            return "{\"error\": \"Unable to delete item, please see server console for more info.\"}";
        }
    }



    /* removes the duplicate code of the data entry into the SQL statement for update() and add(), as there code was very similar */
    private static void fillColumn(int accountID, int managerID, int accessLevel, PreparedStatement ps, int column) throws SQLException {
        ps.setInt(1+column, accountID);
        ps.setInt(2+column, managerID);
        ps.setInt(3+column, accessLevel);
    }

}

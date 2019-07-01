import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static java.lang.System.out;

public class UserController {

    public static void selectUsers(){
        try{
            PreparedStatement ps = main.db.prepareStatement("SELECT * FROM Users");
            ResultSet result = ps.executeQuery();

            while(result.next()){
                String output = "";
                output += result.getInt(1) + " ";
                output += result.getString(2) + " ";
                output += result.getString(3) + " ";
                output += result.getString(4) + " ";
                output += result.getString(5) + " ";
                output += result.getString(6) + " ";
                output += result.getString(7) + " ";
                output += result.getString(8);
                out.println(output);
            }

        } catch (Exception e){
            out.println("Error reading database, error message:\n" + e.getMessage());
        }
    }

    public static void insertUser(int userID, String firstName, String surname, String dateOfBirth, String preferredTitle, String email, String phoneNumber, String password){

        try{
            PreparedStatement ps = main.db.prepareStatement("INSERT INTO Users (UserID, FirstName, Surname, DateOfBirth, PreferredTitle, Email, PhoneNumber, Password) VALUES (?,?,?,?,?,?,?,?)");
            ps.setInt(1,userID);
            ps.setString(2,firstName);
            ps.setString(3,surname);
            ps.setString(4,dateOfBirth);
            ps.setString(5,preferredTitle);
            ps.setString(6,email);
            ps.setString(7,phoneNumber);
            ps.setString(8,password);

            ps.executeUpdate();

        } catch (Exception e){
            out.println("Error when inputting user into database, error code\n" + e.getMessage());
        }

    }

    public static void deleteUser(int userID){
        try{
            PreparedStatement ps = main.db.prepareStatement("DELETE FROM Users WHERE UserID = ?");
            ps.setInt(1,userID);
            ps.execute();

        } catch (Exception e){
            out.println("Error deleting user, error message:\n" + e.getMessage());

        }

    }

    public static void updateUser(String firstName, int userID){
        try{
            PreparedStatement ps = main.db.prepareStatement("UPDATE users SET FirstName = ? WHERE UserID = ?");
            ps.setString(1,firstName);
            ps.setInt(2,userID);
            ps.execute();

        } catch (Exception e){

            out.println("Error updating user, error message:\n" + e.getMessage());
        }

    }

}

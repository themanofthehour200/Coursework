import org.sqlite.SQLiteConfig;

import java.sql.*;

import static java.lang.System.out;

public class main {

    public static Connection db = null;

    public static void main(String[] args) {
        openDatabase("courseworkDatabase.db");
        insertUser(2,"Karen","Lesley","Brown","1970-08-14","Female");
        deleteUser(454);
        selectUsers();
        updateUser("Ben", 1);
        // code to get data from, write to the database etc goes here!
        closeDatabase();
    }

    //establishes a connection to the database
    private static void openDatabase(String dbFile) {
        try  {
            Class.forName("org.sqlite.JDBC");
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);//allows referential integrity
            db = DriverManager.getConnection("jdbc:sqlite:resources/" + dbFile, config.toProperties());
            out.println("Database connection successfully established.");
        } catch (Exception e) {//Gives custom error message without closing program if error occurs
            out.println("Database connection error: " + e.getMessage());
        }

    }

    //Closes the connection with the database
    private static void closeDatabase(){
        try {
            db.close();
            out.println("Disconnected from database.");
        } catch (Exception e) {
            out.println("Database disconnection error: " + e.getMessage());
        }
    }

    private static void selectUsers(){
        try{
            PreparedStatement ps = db.prepareStatement("SELECT * FROM Users");
            ResultSet result = ps.executeQuery();

            while(result.next()){
                String output = "";
                output += result.getInt(1) + " ";
                output += result.getString(2) + " ";
                output += result.getString(3) + " ";
                output += result.getString(4) + " ";
                output += result.getString(5) + " ";
                output += result.getString(6) + " ";
                out.println(output);

            }

        } catch (Exception e){
            out.println("Error reading database, error message:\n" + e.getMessage());
        }
    }

    private static void insertUser(int userID, String firstName, String middleName, String surname, String dateOfBirth, String gender){

        try{
            PreparedStatement ps = db.prepareStatement("INSERT INTO Users (UserID, FirstName, MiddleNames, Surname, DateOfBirth, Gender) VALUES (?,?,?,?,?,?)");
            ps.setInt(1,userID);
            ps.setString(2,firstName);
            ps.setString(3,middleName);
            ps.setString(4,surname);
            ps.setString(5,dateOfBirth);
            ps.setString(6,gender);

            ps.executeUpdate();

        } catch (Exception e){
            out.println("Error when inputting user into database, error code\n" + e.getMessage());
        }

    }

    private static void deleteUser(int userID){
        try{
            PreparedStatement ps = db.prepareStatement("DELETE FROM Users WHERE UserID = ?");
            ps.setInt(1,userID);
            ps.execute();

        } catch (Exception e){
            out.println("Error deleting user, error message:\n" + e.getMessage());
        }
    }

    private static void updateUser(String firstName, int userID){
        try{
            PreparedStatement ps = db.prepareStatement("UPDATE users SET FirstName = ? WHERE UserID = ?");
            ps.setString(1,firstName);
            ps.setInt(2,userID);
            ps.execute();

        } catch (Exception e){
            out.println("Error updating user, error message:\n" + e.getMessage());
        }

    }

}


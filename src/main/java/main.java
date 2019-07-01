import org.sqlite.SQLiteConfig;

import java.sql.*;

import static java.lang.System.out;

public class main {

    public static Connection db = null;

    public static void main(String[] args) {
        openDatabase("courseworkDatabase.db");
        //UserController.insertUser(2,"Callum","Brown","14-08-2005","Master","callum.brown@test.com","01256767718","test");
        UserController.selectUsers();
        //deleteUser();
        //updateUser();
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

}


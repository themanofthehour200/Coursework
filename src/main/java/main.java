import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

import static java.lang.System.out;

public class main {

    public static Connection db = null;

    public static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        openDatabase("courseworkDatabase.db");

        UserController.delete(19);

        closeDatabase();
    }

    //establishes a connection to the database
    private static void openDatabase(String dbFile) {
        try {
            Class.forName("org.sqlite.JDBC");
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);//allows referential integrity
            db = DriverManager.getConnection("jdbc:sqlite:resources/" + dbFile, config.toProperties());
            out.println("Database connection successfully established.");
        } catch (Exception e) {//Gives custom error message without closing program if error occurs
            out.println("Database connection error: " + e.getMessage());
        }

    }

/*    private static void login(){
        out.println("Enter email:");
        String email = sc.nextLine();
        UserController.search();
    }*/

    //Closes the connection with the database
    private static void closeDatabase() {
        try {
            db.close();
            out.println("Disconnected from database.");
        } catch (Exception e) {
            out.println("Database disconnection error: " + e.getMessage());
        }
    }

}


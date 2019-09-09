import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.ArrayList;

import static java.lang.System.out;

public class main {

    public static Connection db = null;

    public static void main(String[] args) {
        openDatabase("courseworkDatabase.db");

        //Testing tings here

        /*
        AccountController.delete(1);
        AccountController.selectAll();
        AccountController.insert(10,"Testers account",500,"USD");
        AccountController.update(10,"testing account", 400, "GDP");
        AccountController.search(10);*/

        out.println("\n");


        /*
        UserController.insert(23,"Callum","Brown","14-08-2005","callum.brown@test.com","01256767718","test");
        UserController.update(23,"Testing","Testing","1990-01-01","test@test.com","01256","###");
        UserController.selectAll();
        UserController.delete(24);*/

        /*TransactionsController.insert(1,10,100,1,"Test","14-08-2005",1);
        TransactionsController.update(1,10,200,1,"Test","14-08-2005",1);
        TransactionsController.selectAll();
        TransactionsController.search(1);
        TransactionsController.delete(1);

        CategoryController.insert(2, "test", 10);
        CategoryController.update(2, "Food", 10);
        CategoryController.search(2);
        CategoryController.insert(3, "test", 10);
        CategoryController.selectAll();
        CategoryController.delete(3);
        CategoryController.selectAll();*/

        //AccountManagersController.insert(1,10,3,2);

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


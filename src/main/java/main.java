import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.out;

public class main {

    public static Connection db = null;

    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        openDatabase("courseworkDatabase.db");

        newUser();

        closeDatabase();
    }

    private static void newUser(){
        out.println("Enter first name:");
        String firstName  = sc.nextLine();
        while(!nameCheck(firstName)){
            out.println("Name must be over one character long and can't contain numbers or special characters");
            out.println("Enter first name:");
            firstName  = sc.nextLine();
        }
        out.println("Password must be between 8 and 16 characters, contain at least one special character and one number");
        out.println("Input: ");
        String password = sc.nextLine();
        while(!passwordCheck(password)){
            out.println("Invalid input, please try again");
            out.println("Password must be between 8 and 16 characters, contain at least one special character and one number");
            out.println("Input: ");
            password = sc.nextLine();
        }
    }
    //Checks first and last names contain no numbers and are at least two characters long
    private static boolean nameCheck(String name){
        Pattern letter = Pattern.compile("[a-zA-z]");//Makes sure name isn't just whitespace
        Pattern digit = Pattern.compile("[0-9]");//checks no numbers in name
        Matcher hasLetter = letter.matcher(name);
        Matcher hasDigit = digit.matcher(name);
        return (hasLetter.find() && !hasDigit.find() && name.length() > 1);//Password can't contains numbers or be one character long
    }

    private static boolean passwordCheck(String password){
        if(password.length() >= 8 && password.length() <= 16){
            Pattern letter = Pattern.compile("[a-zA-z]");//Checks that password contains letters
            Pattern digit = Pattern.compile("[0-9]");//and numbers
            Pattern special = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~-]");//and a special character

            Matcher hasLetter = letter.matcher(password);//Checks string to see if it contains the parameters that were set
            Matcher hasDigit = digit.matcher(password);
            Matcher hasSpecial = special.matcher(password);

            return hasLetter.find() && hasDigit.find() && hasSpecial.find();
        }else{
            return false;
        }
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


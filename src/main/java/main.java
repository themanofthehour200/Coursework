import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.out;

/*encryption*/
/*encryption*/

public class main {

    public static Connection db = null;//For database connection
    private static Scanner sc = new Scanner(System.in);

    private static boolean loggedIn = false;


    public static void main(String[] args) {
        openDatabase("courseworkDatabase.db");
        out.println("test push");

        login();

        closeDatabase();
    }

    private static void newUser(){
        String firstName = nameValid("first ");//Need validation as none in lower levels
        String surname = nameValid("sur");
        out.println("DOB: ");
        String dateOfBirth = sc.nextLine();
        out.println("Email: ");
        String email = sc.nextLine();
        out.println("Enter phone number: ");//Not much validation done on phone numbers as they're optional and vary a lot country to country
        String phoneNumber = sc.nextLine();
        String password = passwordValid();//Validation needed as none at lower levels
        UserController.insert(firstName,surname,dateOfBirth,email,phoneNumber,password);
    }

    private static void login(){
        out.println("Enter email: ");
        String email = sc.nextLine();
        out.println("Enter password");
        String password = sc.nextLine();
        if(UserController.search(0,email,password)!=null)out.println("Login successful!");
        else out.println("Login failed");
    }
    //Checks first and last names contain no numbers and are at least two characters long
    private static String nameValid(String position){
        out.println("Enter "+ position +"name:");
        String name  = sc.nextLine();

        Pattern letter = Pattern.compile("[a-zA-z]");//Checks name contains letters (not just whitespace)
        Pattern digit = Pattern.compile("[0-9]");//Checks no numbers in name
        Pattern special = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~]");//Checks no special characters apart from '-'

        Matcher hasLetter = letter.matcher(name);
        Matcher hasDigit = digit.matcher(name);
        Matcher hasSpecial = special.matcher(name);

        if(!hasLetter.find() || hasDigit.find() || hasSpecial.find() || name.length() < 2) {//Name can't contains numbers or be one character long
            out.println("Name must be over one character long and can't contain numbers or special characters");
            nameValid(position);//Uses recursion to ensure that a valid name is entered
        }
        return name;
    }

    private static String passwordValid(){
        out.println("Password must be between 8 and 16 characters, contain at least one special character and one number");
        out.println("Input: ");
        String password = sc.nextLine();
        if(password.length() >= 8 && password.length() <= 16){
            /*If statement here as this branch should only be done if the length is correct, as this branch is
            complex to process; shouldn't be done if not necessary*/

            Pattern letter = Pattern.compile("[a-zA-z]");//Checks that password contains letters
            Pattern digit = Pattern.compile("[0-9]");//and numbers
            Pattern special = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~-]");//and a special character

            Matcher hasLetter = letter.matcher(password);//Checks string to see if it contains the parameters that were set
            Matcher hasDigit = digit.matcher(password);
            Matcher hasSpecial = special.matcher(password);

            if (!hasLetter.find() || !hasDigit.find() && !hasSpecial.find()){ //If not meeting requirements of a letter,digit and special character
                out.println("Invalid input, please try again");
                passwordValid();
            }
        }else{
            out.println("Invalid input, please try again");
            passwordValid();
        }
        return password;//Will only arrive at this part if password has met requirements
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



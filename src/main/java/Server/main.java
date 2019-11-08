package Server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.out;

public class main {

    public static Connection db = null;//For database connection
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        openDatabase("courseworkDatabase.db");


        ResourceConfig config = new ResourceConfig(); //Prepping our 'Servlet'
        config.packages("Controllers"); //This makes sure that the controller classes can access the HTTp server
        config.register(MultiPartFeature.class); //Makes sure server supports multi-part HTML forms
        ServletHolder servlet = new ServletHolder(new ServletContainer(config)); //Instantiate the Servlet

        Server server = new Server(8081); //Prepping our jetty server to listen on port 8081
        ServletContextHandler context = new ServletContextHandler(server, "/"); //Instantiate the server
        context.addServlet(servlet, "/*"); //Connect the servlet to the server

        try {
            server.start(); //Start the server
            out.println("Server successfully started."); //Helpful message for development
            server.join(); //Means program will run indefinitely, as it is joining the main thread
        } catch (Exception e) {
            e.printStackTrace(); //Error catching and handling here
        }
        closeDatabase();
    }


    //Checks first and last names contain no numbers and are at least two characters long
    public static boolean nameValid(String name){

        Pattern letter = Pattern.compile("[a-zA-z]");//Checks name contains letters (not just whitespace)
        Pattern digit = Pattern.compile("[0-9]");//Checks no numbers in name
        Pattern special = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~]");//Checks no special characters apart from '-'

        Matcher hasLetter = letter.matcher(name);
        Matcher hasDigit = digit.matcher(name);
        Matcher hasSpecial = special.matcher(name);

        //Name can't contains numbers or be one character long
        return (hasLetter.find() && !hasDigit.find() && !hasSpecial.find() && name.length() >= 2);
    }

    public static boolean emailValid(String email){
        Pattern pattern = Pattern.compile("^.+@.+\\..+$");
        Matcher matcher = pattern.matcher(email);
        return matcher.find();
    }

    public static boolean passwordValid(String password){
        if(password.length() >= 8 && password.length() <= 16){
            //If statement here as this branch should only be done if the length is correct, as this branch is
            //complex to process; shouldn't be done if not necessary

            Pattern lowerLetter = Pattern.compile("[a-z]");//Checks that password contains lower case
            Pattern upperLetter = Pattern.compile("[A-z]");//Checks that password contains upper case
            Pattern digit = Pattern.compile("[0-9]");//and numbers
            Pattern special = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~-]");//and a special character

            Matcher hasLowerLetter = lowerLetter.matcher(password);//Checks string to see if it contains the parameters that were set
            Matcher hasUpperLetter = upperLetter.matcher(password);//Checks string to see if it contains the parameters that were set
            Matcher hasDigit = digit.matcher(password);
            Matcher hasSpecial = special.matcher(password);

            return (hasLowerLetter.find() && hasUpperLetter.find() && hasDigit.find() && hasSpecial.find());//If not meeting requirements of a letter,digit and special character

        }else return false;
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



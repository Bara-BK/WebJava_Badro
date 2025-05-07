package tn.badro.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    public final String URL="jdbc:mysql://localhost:3306/jacemdb";
    public final String USER="root";
    public final String PWD="";
    private static MyDataBase myDataBase;
    
    private MyDataBase() {
        // Empty constructor - connections are created on demand
    }

    public static MyDataBase getInstance() {
        if (myDataBase==null) {
            myDataBase=new MyDataBase();
        }
        return myDataBase;
    }

    public Connection getCnx() {
        try {
            // Create a new connection each time
            Connection cnx = DriverManager.getConnection(URL, USER, PWD);
            return cnx;
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

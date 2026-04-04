package li.mtu.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/*
* Database system: SQLite
Info needed for project
* Subject of course
* Course number
* Name of course
* Description of course
* Credit count of course
* Prerequisites
* Corequisites
* Attributes

* */
public class Database {
    Connection connection;
    Statement statement;

    public Database(String path) throws SQLException {
        // Connect to database
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        statement = connection.createStatement();
    }

    // Create the necessary tables to store the data
    private void createTables() {
        // Terms table
//        statement.executeUpdate();
//
//        // Courses table
//        statement.executeUpdate();
//
//        // Sections table
//        statement.executeUpdate();
//
//        // Instructors table
//        statement.executeUpdate();
//
//        // Attributes table
//        statement.executeUpdate();
    }
}

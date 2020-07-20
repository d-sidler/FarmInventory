package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {

    public static Connection getConnection() throws SQLException {

        final String SQCONN = "jdbc:sqlite:farmInventory.sqlite";

        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection(SQCONN);
            return connection;
        } catch (ClassNotFoundException e) {
            return null;
        }

    }

    public static void initializeTables() {

        Connection connection = null;
        try {
            connection = getConnection();
        }
        catch(SQLException e) {
            e.printStackTrace();
        }

        if (connection != null) {
            initializeCustomerTable(connection);
            initializeItemsTable(connection);
            initializeOrdersTable(connection);
        }

    }

    private static void initializeCustomerTable(Connection connection) {

        // initialize Customer Table if not exists
        String sqlGenCustomer = "CREATE TABLE IF NOT EXISTS customers (" +
                "id         INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "firstName  TEXT," +
                "lastName   TEXT," +
                "email      TEXT" +
                ");";

        try {
            PreparedStatement st = connection.prepareStatement(sqlGenCustomer);
            st.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initializeItemsTable(Connection connection) {

        // initialize Items Table if not exists
        String sqlGenItem = "CREATE TABLE IF NOT EXISTS items (" +
                "id            INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "itemName      TEXT," +
                "price         DOUBLE," +
                "priceUnit     TEXT" +
                ");";

        try {
            PreparedStatement st = connection.prepareStatement(sqlGenItem);
            st.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static void initializeOrdersTable(Connection connection) {


        // initialize order Table if not exists
        String sqlGenOrder = "CREATE TABLE IF NOT EXISTS orders (" +
                "id            INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customerID    INTEGER," +
                "itemID        INTEGER," +
                "orderDate     TEXT," +
                "harvestDate   TEXT" +
                ");";

        try {
            PreparedStatement st = connection.prepareStatement(sqlGenOrder);
            st.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }


    }



}

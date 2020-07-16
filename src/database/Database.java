package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {

    public Database() {

        try {
            connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        initializeTables();
    }

    public Connection getConnection() {
        return connection;
    }

    private Connection connection;

    private void connect() throws SQLException {

        final String SQCONN = "jdbc:sqlite:farmInventory.sqlite";

        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(SQCONN);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void initializeTables() {

        initializeCustomerTable();
        initializeItemsTable();
        initializeOrdersTable();

    }

    private void initializeCustomerTable() {

        // initialize Customer Table if not exists
        String sqlGenCustomer = "CREATE TABLE IF NOT EXISTS customers (" +
                "id         INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "firstName  TEXT," +
                "lastName   TEXT," +
                "email      TEXT" +
                ");";

        try {
            PreparedStatement st = this.connection.prepareStatement(sqlGenCustomer);
            st.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeItemsTable() {

        // initialize Items Table if not exists
        String sqlGenItem = "CREATE TABLE IF NOT EXISTS items (" +
                "id            INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "itemName      TEXT," +
                "price         DOUBLE," +
                "priceUnit     TEXT," +
                "pricePerUnit  TEXT" +
                ");";

        try {
            PreparedStatement st = this.connection.prepareStatement(sqlGenItem);
            st.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void initializeOrdersTable() {


        // initialize order Table if not exists
        String sqlGenOrder = "CREATE TABLE IF NOT EXISTS orders (" +
                "id            INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customerID    INTEGER," +
                "itemID        INTEGER," +
                "orderDate     TEXT," +
                "harvestDate   TEXT" +
                ");";

        try {
            PreparedStatement st = this.connection.prepareStatement(sqlGenOrder);
            st.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }


    }


}

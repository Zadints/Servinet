package org.example.servinet.config;

import net.bytebuddy.asm.Advice;

import java.io.ObjectInputFilter;
import java.sql.*;

public class LoadDb {

    private static Connection conn;

    public static void startConnection(){

        try {
            String host = ConfigLoad.getYmlHost();
            int port = ConfigLoad.getYmlPort();
            String database = ConfigLoad.getYmlName();
            String username = ConfigLoad.getYmlUsername();
            String password = ConfigLoad.getYmlPassword();

            String url = String.format(
                    "jdbc:sqlserver://%s:%d;" +
                            "databaseName=%s;" +
                            "encrypt=true;" +
                            "trustServerCertificate=true",
                    host,
                    port,
                    database
            );

            conn = DriverManager.getConnection(
                    url,
                    username,
                    password
            );
        }catch (SQLException e) {
            return;
        }


    }
    public static void closeConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    public static Connection getConnection(){

        try {
            if (conn == null || conn.isClosed()) {
                startConnection();
            }
        }catch (SQLException e) {

            return null;
        }
        return conn;
    }
}

package org.example.servinet.infrastructure.database.deploy.factory;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseFactory {
    Connection getConnection() throws SQLException;
}
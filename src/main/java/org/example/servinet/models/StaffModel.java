package org.example.servinet.models;

import org.example.servinet.config.LoadDb;
import org.example.servinet.core.entities.Staff;
import java.sql.*;

public class StaffModel {

    public static Staff getUserDatabase() {

        Connection conn = LoadDb.getConnection();


        try (
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM personal");
                ResultSet rs = stmt.executeQuery()
        ){

            if (rs.next()) {

                Staff user = new Staff();

                /*
                user.setUserHandle(rs.getString("user_handle"));
                user.setUserDisplayName(rs.getString("user_displayname"));
                user.setUserEmail(rs.getString("user_email"));
                */
                return user;
            }

        } catch (SQLException e) {
        }

        return null;
    }


}
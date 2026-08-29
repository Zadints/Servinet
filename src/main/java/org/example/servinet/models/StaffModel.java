package org.example.servinet.models;

import org.example.servinet.config.LoadDb;
import org.example.servinet.core.entities.Staff;
import java.sql.*;

public class StaffModel {

    public static Staff getUserDatabase() {
        String sql = "SELECT * FROM staff WHERE staff_name = ?";
        Connection conn = LoadDb.getConnection();

        /*
        try (
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM staff WHERE staff_name = ?");
                ResultSet rs = stmt.executeQuery()
        ){

            if (rs.next()) {


                String rs.getString("user_handle")

                Staff user = new Staff();


                user.setUserHandle();
                user.setUserDisplayName(rs.getString("user_displayname"));
                user.setUserEmail(rs.getString("user_email"));

                return user;
            }

        } catch (SQLException e) {
        }
        */
        return null;
    }


}
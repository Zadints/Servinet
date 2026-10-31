package org.example.servinet.application.port.out;

import org.example.servinet.application.dto.database.AuthLogin;
import org.example.servinet.domain.entities.Role;
import org.example.servinet.domain.entities.Staff;

import java.util.List;

public interface StaffRepository {

    String getStaffActiveHardwareId(String hardwareId);

    Staff getStaffAndNewSession(String hardwareId, String staffUuid);

    AuthLogin getStaffAuthLoginByEmail(String email);

    void removeStaffSession(String hardwareId);


    Role getStaffRole(String roleUuid);

    int countStaffWithRole(String roleUuid);


    void setStaffDatabase(Staff staff, String password);

    Staff getStaffByUuid(String staffUuid);

    List<Staff> getAllStaff();

    boolean isNameOrEmailTaken( String email);

    void updateStaff(
            String uuid, String email,
            String roleUuid,
            String newPasswordHash,
            byte[] imageBytes
    );

    void deleteStaff(String uuid);

    void updatePassword(String uuid, String newPasswordHash);
}



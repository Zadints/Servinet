package org.example.servinet.application.port.out.sql;

import org.example.servinet.domain.entities.AppGeneral;

public interface AppRepository {

    AppGeneral get();

    void renameApp(String name);
    void changueImage(byte[] image);
}

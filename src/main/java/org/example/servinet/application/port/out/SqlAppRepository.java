package org.example.servinet.application.port.out;

import org.example.servinet.domain.entities.AppGeneral;

public interface SqlAppRepository {

    AppGeneral get();

    void renameApp(String name);
    void changueImage(byte[] image);
}

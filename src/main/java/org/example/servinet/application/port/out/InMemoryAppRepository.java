package org.example.servinet.application.port.out;

import org.example.servinet.domain.entities.AppGeneral;

public interface InMemoryAppRepository {
    void set(AppGeneral entity);
    AppGeneral get();
    void updateName(String name);
    void updateImage(byte[] image);
}

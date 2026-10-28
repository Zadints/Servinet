package org.example.servinet.infrastructure.database.inmemory;


import org.example.servinet.application.port.out.InMemoryAppRepository;
import org.example.servinet.domain.entities.AppGeneral;

public class InMemoryAppSettingRepository implements InMemoryAppRepository {


    private AppGeneral appGeneral;

    @Override
    public void set(AppGeneral entity) {
        appGeneral = entity;
    }

    @Override
    public AppGeneral get() {
        return appGeneral;
    }

    @Override
    public void updateName(String name) {
        appGeneral.setAppName(name);
    }

    @Override
    public void updateImage(byte[] image) {
        appGeneral.setLogoApp(image);
    }

}

package org.example.servinet.application.service;

import org.example.servinet.application.dto.AttentionLog;
import org.example.servinet.application.port.out.sql.Sellerrepository;
import org.example.servinet.domain.entities.AttentionStatistics;
import org.example.servinet.domain.exception.InvalidValueException;

import java.util.List;

public class SellerService {
    private final Sellerrepository sqlSellerrepository;

    public SellerService(Sellerrepository sqlSellerrepository) {
        this.sqlSellerrepository = sqlSellerrepository;
    }

    public void register(AttentionLog atn) {

        if (atn == null) {
            throw new InvalidValueException("La información de atención es obligatoria");
        }

        if (atn.staffUuid() == null) {
            throw new InvalidValueException("El UUID del trabajador es obligatorio");
        }

        if (atn.clientName() == null || atn.clientName().isBlank()
                || atn.clientName().length() > 100) {
            throw new InvalidValueException("El nombre del cliente no es válido");
        }

        if (atn.clientLastName() == null || atn.clientLastName().isBlank()
                || atn.clientLastName().length() > 100) {
            throw new InvalidValueException("El apellido del cliente no es válido");
        }

        if (atn.clientDni() != null
                && !atn.clientDni().matches("\\d{8}")) {
            throw new InvalidValueException("El DNI debe contener exactamente 8 dígitos");
        }

        if (atn.atencionTipo() == null) {
            throw new InvalidValueException("El tipo de atención es obligatorio");
        }

        if (atn.valoracion() < 1 || atn.valoracion() > 5) {
            throw new InvalidValueException("La valoración debe estar entre 1 y 5");
        }

        if (atn.descriptionTipo() != null
                && atn.descriptionTipo().length() > 200) {
            throw new InvalidValueException("La descripción no puede superar los 200 caracteres");
        }

        sqlSellerrepository.registerAttentionLog(atn);
    }

    public List<AttentionStatistics> getAttentionStatistics(){
       return   sqlSellerrepository.getAttentionStatistics();
    }

}

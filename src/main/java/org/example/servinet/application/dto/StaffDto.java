package org.example.servinet.application.dto;

import org.example.servinet.domain.entities.Role;

//Si vas a editar entidad también edita SQL SERVER :DDD

public record StaffDto(
        String dni,
        String name,
        String lastName,
        String phone,
        Role rol,
        String email
){
}

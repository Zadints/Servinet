package org.example.servinet.application.dto.staff;

import org.example.servinet.domain.entities.Role;

//Si vas a editar entidad también edita SQL SERVER :DDD
//no hace falta bro aqui solo agregaré que peuda obtener password, editaré jdbc

public record StaffDto(
        String dni,
        String name,
        String password,
        String lastName,
        String phone,
        Role rol,
        String email
){
}

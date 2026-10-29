package org.example.servinet.application.usecase.staff;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.example.servinet.application.dto.UserDto;
import org.example.servinet.application.dto.database.AuthLogin;
import org.example.servinet.application.port.out.*;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.exception.InvalidCredentialsException;

import java.time.LocalDateTime;
import java.util.UUID;

public class RegisterStaffUseCase {

    private final StaffRepository sqlStaffAuthRepository;
    private final InMemoryComplexRepository<Staff> inMemoryRepository;
    private final Argon2 argon;
    private final PasswordGenerator passwordGenerator;

    public RegisterStaffUseCase(StaffRepository sqlStaffAuthRepository, InMemoryComplexRepository<Staff> staffRepository, PasswordGenerator passwordGenerator) {
        this.sqlStaffAuthRepository = sqlStaffAuthRepository;
        this.inMemoryRepository = staffRepository;
        this.passwordGenerator = passwordGenerator;
        this.argon = Argon2Factory.create();
    }


    public void register(UserDto userDto) {

        AuthLogin dto = sqlStaffAuthRepository.getStaffAuthLoginByEmail(userDto.getEmail());

        if (dto != null){
            throw new InvalidCredentialsException(
                    "no puedes registrar el mismo usuario dos veces"
            );
        }

        String uuid = UUID.randomUUID().toString();
        System.out.println("Creando nuevo usuario en lista" + userDto.getName());
        Staff newStaff = new Staff(
                uuid,
                userDto.getEmail(),
                userDto.getRol(),
                LocalDateTime.now(),
                userDto.getName(),
                userDto.getPerfilImg()
        );
        inMemoryRepository.addEntity(newStaff);

        Thread thread = new Thread(() -> {

            String password = passwordGenerator.generate(9);

            sqlStaffAuthRepository.setStaffDatabase(newStaff, hashPassword(password));
            inMemoryRepository.addEntity(newStaff);
            //enviar aqui el password por gmail;
        });

        thread.setDaemon(true);
        thread.start();
    }

    private String hashPassword(String password){
        return argon.hash(3, 65536, 1, password);
    }
}

package org.example.servinet.application.usecase.staff;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import javafx.concurrent.Task;
import org.example.servinet.application.dto.StaffDto;
import org.example.servinet.application.dto.database.AuthLogin;
import org.example.servinet.application.port.out.*;
import org.example.servinet.application.service.WhatsAppService;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.exception.ApiException;
import org.example.servinet.domain.exception.InvalidCredentialsException;

import java.time.LocalDateTime;
import java.util.UUID;

public class RegisterStaffUseCase {

    private final StaffRepository sqlStaffAuthRepository;
    private final InMemoryComplexRepository<Staff> inMemoryRepository;
    private final Argon2 argon;
    private final PasswordGenerator passwordGenerator;
    private final WhatsAppService whatsAppService;
    private final AppExecutor appExecutor;

    public RegisterStaffUseCase(StaffRepository sqlStaffAuthRepository, InMemoryComplexRepository<Staff> staffRepository, PasswordGenerator passwordGenerator, WhatsAppService whatsAppService, AppExecutor appExecutor) {
        this.sqlStaffAuthRepository = sqlStaffAuthRepository;
        this.inMemoryRepository = staffRepository;
        this.passwordGenerator = passwordGenerator;
        this.whatsAppService = whatsAppService;
        this.appExecutor = appExecutor;
        this.argon = Argon2Factory.create();
    }


    public void register(StaffDto userDto) {



        AuthLogin dto = sqlStaffAuthRepository.getStaffAuthLoginByEmail(userDto.email());

        if (dto != null){
            throw new InvalidCredentialsException(
                    "no puedes registrar el mismo usuario dos veces"
            );
        }

        String uuid = UUID.randomUUID().toString();
        System.out.println("Creando nuevo usuario en lista" + userDto.name());
        Staff newStaff = new Staff(
                uuid,
                userDto.phone(),
                userDto.email(),
                userDto.rol(),
                LocalDateTime.now(),
                userDto.name(),
                null
        );
        String password = passwordGenerator.generate(9);

        sqlStaffAuthRepository.setStaffDatabase(newStaff, hashPassword(password));


        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                inMemoryRepository.addEntity(newStaff);

                try {
                    whatsAppService.sendMessage(userDto.phone(),
                            """
                        🤖 *SERVINET BOT*
            
                        👋 ¡Hola! Como medida de seguridad creamos tu contraseña
                            para que peudas acceder a la aplicación!
            
                        ⚡ *Contraseña:* %s
            
                        Gracias por trabajar con nosotros. 🚀
                        """.formatted(password)
                    );
                } catch(ApiException e){
                    System.out.println(e.getMessage());
                }
                return null;
            }

        };
        appExecutor.execute(task);

    }

    private String hashPassword(String password){
        return argon.hash(3, 65536, 1, password);
    }
}

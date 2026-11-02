package org.example.servinet.application.usecase.clients;

import javafx.concurrent.Task;
import org.example.servinet.application.service.ClientService;
import org.example.servinet.application.service.StaffCacheService;
import org.example.servinet.application.service.WhatsAppService;
import org.example.servinet.application.usecase.staff.GetStaffUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.Client;
import org.example.servinet.domain.entities.Staff;
import org.example.servinet.domain.exception.ApiException;

public class CallClientUseCase {
    private final WhatsAppService whatsAppService;
    private final StaffCacheService getStaffUseCase;
    private final AppExecutor appExecutor;

    public CallClientUseCase(WhatsAppService whatsAppService, StaffCacheService getStaffUseCase1, AppExecutor appExecutor) {

        this.whatsAppService = whatsAppService;
        this.getStaffUseCase = getStaffUseCase1;

        this.appExecutor = appExecutor;
    }

    public void sendCall(Client client){
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                System.out.println("obteniendo msg");
                Staff staff = getStaffUseCase.getCurrent();
                try {
                    System.out.println("Enviando...");
                    whatsAppService.sendMessage(staff.getPhone(),
                            """
                        🤖 *SERVINET BOT*
            
                        👋 ¡Hola! %s
                            El número del cliente %s es:
                            %s
            
                        Ignora este mensaje si es el numero incorrecto.🚀
                        """.formatted(staff.getDisplay(), client.getFirstName(), client.getPhone())
                    );
                } catch(Exception e){
                    System.out.println(e.getMessage() + e.getCause());
                }
                return null;
            }

        };
        appExecutor.execute(task);
    }
}

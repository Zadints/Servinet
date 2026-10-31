package org.example.servinet.application.usecase;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.servinet.application.port.out.InMemoryRepository;
import org.example.servinet.application.port.out.sql.ClientRepository;
import org.example.servinet.domain.entities.ClientPlan;

import java.util.List;

public class PlanUseCase {

    private final ClientRepository sqlClientAuthRepository;
    private final InMemoryRepository<ClientPlan> inMemoryPlansRepository;


    public PlanUseCase(ClientRepository sqlClientAuthRepository,
                       InMemoryRepository<ClientPlan> inMemoryPlansRepository) {
        this.sqlClientAuthRepository = sqlClientAuthRepository;
        this.inMemoryPlansRepository = inMemoryPlansRepository;
    }

    public List<ClientPlan> getPool() {
        return inMemoryPlansRepository.getAll();
    }

    public ObservableList<ClientPlan> getAll() {

        List<ClientPlan> plans = inMemoryPlansRepository.getAll();

        if (plans != null) {
            return FXCollections.observableArrayList(plans);
        }

        plans = sqlClientAuthRepository.getAllPlansDatabase();

        if (plans == null) {
            return null;
        }
        //cargar del sql los planes
        return  FXCollections.observableArrayList(plans);
    }

    // Valida por objeto ClientPlan
    public boolean existPlans(ClientPlan plan) {
        if (plan == null || plan.getId() == null) {
            return false;
        }
        return existPlans(plan.getId());
    }

    // Sobrecarga: valida por ID de plan
    public boolean existPlans(String planId) {

        List<ClientPlan> plans = inMemoryPlansRepository.getAll();

        if (planId == null || planId.isBlank() || plans.isEmpty()) {
            return false;
        }
        return plans.stream()
                .anyMatch(p -> p.getId().equalsIgnoreCase(planId));
    }

    public void addPlan(ClientPlan plan) {
        if (plan != null && !existPlans(plan.getId())) {
            inMemoryPlansRepository.addEntity(plan);
        }
    }
    //Procedimiento almacenado ,,,, no sabia donde ponerlo

    //CREATE OR ALTER PROCEDURE sp_GetAllClientsOrderReport
     //       AS
    //BEGIN
    //SET NOCOUNT ON;

    //SELECT
    //c.dni               AS clientDni,
    //c.email             AS clientEmail,
    //c.first_name        AS clientFirstName,
    //c.last_name         AS clientLastName,
    //c.phone             AS clientPhone,
    //c.address           AS clientAddress,
    //c.client_status     AS clientStatus,
    //c.payment_status    AS paymentStatus,
    //p.id_plan           AS planId,
    //p.plan_name         AS planName,
    //p.speed             AS planSpeed,
    //p.price             AS planPrice,
    //o.start_install     AS orderStartInstall,
    //o.created_at        AS orderCreate,
    //o.img_router        AS imgRouter
    //FROM clients c
    //INNER JOIN orders o ON c.dni = o.client_dni
    //INNER JOIN plans p  ON o.id_plan = p.id_plan
    //ORDER BY o.created_at DESC;
   // END;
    //GO//
    //

    //para solo clientes podria ser :
    //CREATE OR ALTER PROCEDURE ""nombre de la lsita de clientes""
    //    @Dni            VARCHAR(8),
    //    @Email          VARCHAR(100),
    //    @FirstName      VARCHAR(100),
    //    @LastName       VARCHAR(100),
    //    @Phone          VARCHAR(15),
    //    @Whatsapp       VARCHAR(15),
    //    @Address        VARCHAR(255),
    //    @PlanId         VARCHAR(50),
    //    @ClientStatus   VARCHAR(20),
    //    @PaymentStatus  VARCHAR(20)
    //AS
    //BEGIN
    //    SET NOCOUNT ON;
    //    BEGIN TRY
    //        BEGIN TRANSACTION;
    //
    //        -- 1. Insertar o actualizar datos del cliente si no existe
    //        IF NOT EXISTS (SELECT 1 FROM clients WHERE dni = @Dni)
    //        BEGIN
    //            INSERT INTO clients (dni, email, first_name, last_name, phone, whatsapp, address, client_status, payment_status)
    //            VALUES (@Dni, @Email, @FirstName, @LastName, @Phone, @Whatsapp, @Address, @ClientStatus, @PaymentStatus);
    //        END
    //
    //        -- 2. Registrar la orden vinculada al plan
    //        INSERT INTO orders (client_dni, id_plan, created_at, order_status)
    //        VALUES (@Dni, @PlanId, GETDATE(), 'PENDIENTE');
    //
    //        COMMIT TRANSACTION;
    //    END TRY
    //    BEGIN CATCH
    //        IF @@TRANCOUNT > 0
    //            ROLLBACK TRANSACTION;
    //
    //        THROW;
    //    END CATCH
    //END;
    //GO
}

package org.example.servinet.core.application.usecase;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.servinet.core.application.dto.OrderReportDto;
import org.example.servinet.core.domain.entities.ClientPlan;
import org.example.servinet.infrastructure.database.models.ClientModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlanUseCase {
    private static List<ClientPlan> plans = new ArrayList<>();

    public static void loadAllPlans() {

        plans = ClientModel.getAllPlansDatabase();

        for (ClientPlan p : plans){
            System.out.println(p.toString());
        }
    }

    public static List<ClientPlan> getPoolPlans() {
        return Collections.unmodifiableList(plans);
    }

    public static ObservableList<ClientPlan> getAllPlans() {
        return FXCollections.observableArrayList(plans);
    }

    public static ClientPlan getPlanById(String planId) {
        if (planId == null || plans.isEmpty()) {
            return null;
        }

        for (ClientPlan p:plans){
            System.out.println("======================== BUCANDO...");
            System.out.println(p.toString());
        }

        return plans.stream()
                .filter(p -> p.getId().equalsIgnoreCase(planId))
                .findFirst()
                .orElse(null);
    }

    // Valida por objeto ClientPlan
    public static boolean existPlans(ClientPlan plan) {
        if (plan == null || plan.getId() == null) {
            return false;
        }
        return existPlans(plan.getId());
    }

    // Sobrecarga: valida por ID de plan
    public static boolean existPlans(String planId) {
        if (planId == null || planId.isBlank() || plans.isEmpty()) {
            return false;
        }
        return plans.stream()
                .anyMatch(p -> p.getId().equalsIgnoreCase(planId));
    }

    public static void addPlan(ClientPlan plan) {
        if (plan != null && !existPlans(plan.getId())) {
            plans.add(plan);
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

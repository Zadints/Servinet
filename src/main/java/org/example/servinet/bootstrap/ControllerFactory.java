package org.example.servinet.bootstrap;
import javafx.util.Callback;
import org.example.servinet.ui.animations.AnimationFactory;
import org.example.servinet.ui.controllers.IndexController;
import org.example.servinet.ui.controllers.LoginController;
import org.example.servinet.ui.controllers.center.*;
import org.example.servinet.ui.controllers.components.*;
import org.example.servinet.ui.controllers.forms.*;
import org.example.servinet.ui.controllers.forms.VentasFormController;
import org.example.servinet.ui.navigator.Navigator;

public class ControllerFactory implements Callback<Class<?>, Object> {

    private final AppCompositionRoot app;
    private Navigator navigator;
    private final AnimationFactory animationFactory;

    public ControllerFactory(AppCompositionRoot appCompositionRoot, AnimationFactory animationFactory) {
        this.app = appCompositionRoot;
        this.animationFactory = animationFactory;
    }

    @Override
    public Object call(Class<?> type) {

        /* pages */
        if (type == LoginController.class){
            return new LoginController(
                app.getAppExecutor(),
                app.getLoginStaffUseCase(),
                navigator

            );
        }
        if (type == IndexController.class){
            return new IndexController(
                    app.getAppExecutor(),
                    navigator,
                    app.getGetStaffUseCase(),
                    app.getAuthorizationSecurity()

            );
        }

        if (type == LogsController.class){
            return new LogsController(
                    app.getGetLogUseCase()
            );
        }

        if (type == LogEntryController.class){
            return new LogEntryController(
            );
        }

        /* center */
        if (type == DashboardController.class){
            return new DashboardController(
                    app.getAppExecutor(),
                    navigator,
                    app.getAuthorizationSecurity(),
                    app.getGetClientUseCase(),
                    app.getGetStaffUseCase(),
                    app.getGetAntennaUseCase(),
                    app.getGetPaymentUseCase(),
                    animationFactory.createShimmerEffectAnimation()

            );
        }
        if (type == AdministrationController.class){
            return new AdministrationController(
                    app.getAppExecutor(),
                    navigator,
                    app.getAuthorizationSecurity(),
                    app.getGetStaffUseCase(),
                    app.getDeleteStaffUseCase(),
                    app.getGetRoleUseCase(),
                    app.getGetLogUseCase()

            );
        }
        if (type == AntenasController.class){
            return new AntenasController(
                    app.getAppExecutor(),
                    navigator,
                    app.getAuthorizationSecurity(),
                    app.getGetAntennaUseCase(),
                    animationFactory.createLoading(),
                    animationFactory.createSlideUp(),
                    app.getDeleteAntennaUseCase(),
                    app.getMaintenanceAntennaUseCase(),
                    app.getGetClientUseCase()
            );
        }
        if (type == OfertasCardController.class) {
            return new OfertasCardController();
        }
        if (type == AnunciosController.class){
            return new AnunciosController(
                    app.getAppExecutor(),
                    navigator,
                    app.getAuthorizationSecurity(),
                    app.getGetAnnounceUseCase(),
                    app.getEventsAnnounceUseCase(),
                    animationFactory.createLoading(),
                    animationFactory.createSlideUp()
            );
        }
        if (type == BackupsController.class){
            return new BackupsController(
                    app.getAppExecutor(),
                    navigator,
                    app.getAuthorizationSecurity()
            );
        }
        if (type == ClientesController.class){
            return new ClientesController(
                    app.getAppExecutor(),
                    navigator,
                    app.getAuthorizationSecurity(),
                    app.getGetClientUseCase(),
                    animationFactory.createLoading(),
                    app.getDeleteClientUseCase(),
                    app.getPlanUseCase()
            );
        }
        if (type == TecnicosController.class){
            return new TecnicosController(
                    app.getTechnicalUseCase(),
                    navigator,
                    app.getPlanUseCase()
            );
        }
        if (type == VentasFormController.class){
            return new VentasFormController(
                    app.getPlanUseCase(),
                    app.getGetAntennaUseCase(),
                    navigator,
                    app.getConsultDniUseCase(),
                    app.getAppExecutor(),
                    app.getCreateClientUseCase(),
                    app.getLocationPicker()
            );
        }
        if (type == VentasMenuController.class){
            return new VentasMenuController(
                    navigator,
                    app.getGetClientUseCase(),
                    app.getPlanUseCase(),
                    app.getAppExecutor());
        }
        /* components */
        if (type == UserRowController.class){
            return new UserRowController(
                    app.getGetStaffUseCase(),
                    app.getAuthorizationSecurity(),
                    navigator

            );
        }
        if (type == AnnuncioCardController.class){
            return new AnnuncioCardController(
                    app.getEventsAnnounceUseCase(),
                    app.getGetAnnounceUseCase(),
                    app.getGetStaffUseCase()
            );
        }
        if (type == SellClientController.class){
            return new SellClientController(
                    app.getCallClientUseCase(),
                            app.getPlanUseCase(),
                    app.getTechnicalUseCase()
            );
        }
        if (type == PlanManagementController.class){
            return new PlanManagementController(
                    app.getPlanUseCase()
            );
        }

        if (type == CreateStaffController.class){
            return new CreateStaffController(
                    app.getRegisterStaffUseCase(),
                    app.getConsultDniUseCase(),
                    navigator,
                    app.getAppExecutor(),
                    app.getGetRoleUseCase()
            );
        }
        if (type == EditStaffController.class){
            return new EditStaffController(
                    app.getEditStaffUseCase(),
                    navigator,
                    app.getGetRoleUseCase()
            );
        }

        if (type == ErrorPageController.class){
            return new ErrorPageController();
        }

        if (type == AppRenameController.class){
            return new AppRenameController(app.getAppGeneralUseCase(),navigator);
        }

        if (type == RolesPermisosController.class){
            return new RolesPermisosController(
                    navigator
            );
        }
        if (type == ClientEditController.class){
            return new ClientEditController(
                    app.getEditClientUseCase(),
    app.getPlanUseCase()
            );
        }

        if (type == PayFormController.class){
            return new PayFormController(
                    app.getPaidPaymentUseCase(),
                    app.getGetClientUseCase(),
                    app.getAppExecutor(),
                    app.getPlanUseCase());
        }

        if (type == AntennaCreateController.class){
            return new AntennaCreateController(
                    app.getCreateAntennaUseCase()

            );
        }

        if (type == AntennaCarruselController.class){
            return new AntennaCarruselController();
        }

        if (type == AntennaMaintenanceController.class){
            return new AntennaMaintenanceController(app.getMaintenanceAntennaUseCase());
        }

        if (type == AntennasEditController.class){
            return new AntennasEditController(app.getEditAntennaUseCase(), navigator);
        }
        if (type == ClienteRowController.class){
            return new ClienteRowController();
        }
        if (type == TechnicalCardController.class) {
            return new TechnicalCardController();
        }

        if (type == InstallationOrdersController .class) {
            return new InstallationOrdersController (
                    app.getGetClientUseCase(),
                    app.getGetAntennaUseCase(),
                    app.getPlanUseCase(),
                    app.getAppExecutor(),
                    app.getTechnicalUseCase()
            );
        }

        if (type == AnnounceCreateController.class) {
            return new AnnounceCreateController(
                    app.getCreateAnnounceUseCase(),
                    app.getEditAnnounceUseCase(),
                    app.getGetAnnounceUseCase()
            );
        }

        if (type == InventarioController.class) {
            return new InventarioController(
                    app.getCreateInventoryUseCase(),
                    app.getUpdateInventoryUseCase(),
                    app.getGetInventoryUseCase(),
                    app.getManageInventoryStockUseCase(),
                    app.getManageInventoryEquipmentUseCase(),
                    app.getGetStaffUseCase(),
                    app.getAppExecutor(),
                    animationFactory.createShimmerEffectAnimation()

            );
        }

        throw new IllegalArgumentException("Controlador desconocido" + type);
    }

    public void setNavigator(Navigator navigator){
        this.navigator =navigator;
    }
}

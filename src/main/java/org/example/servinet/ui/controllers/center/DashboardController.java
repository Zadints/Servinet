package org.example.servinet.ui.controllers.center;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.example.servinet.application.security.AuthorizationSecurity;
import org.example.servinet.application.usecase.antenna.GetAntennaUseCase;
import org.example.servinet.application.usecase.clients.GetClientUseCase;
import org.example.servinet.application.usecase.payments.GetPaymentUseCase;
import org.example.servinet.application.usecase.staff.GetStaffUseCase;
import org.example.servinet.bootstrap.AppExecutor;
import org.example.servinet.domain.entities.client.Payment;
import org.example.servinet.ui.dto.DashDto;
import org.example.servinet.ui.animations.ShimmerEffectAnimation;
import org.example.servinet.ui.navigator.Navigator;

public class DashboardController {

    private boolean mostrandoGrafico;

    @FXML private VBox incomeView;

    @FXML private VBox vboxSellAll;
    @FXML private VBox vboxStaffCount;
    @FXML private VBox vboxClientCount;
    @FXML private VBox vboxAntennaActive;

    @FXML private Label lblSellAllMoney;
    @FXML private Label lblClientsCount;
    @FXML private Label lblStaffCount;
    @FXML private Label lblAntennasActive;
    @FXML private LineChart<String, Number> incomeChart;


    private final AppExecutor appExecutor;
    private final Navigator navigator;
    private final AuthorizationSecurity authorizationService;
    private final GetClientUseCase getClientUseCase;
    private final GetStaffUseCase getStaffUseCase;
    private final GetAntennaUseCase getAntennaUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final ShimmerEffectAnimation shimmerEffectAnimation;

    public DashboardController(AppExecutor appExecutor, Navigator navigator, AuthorizationSecurity authorizationService, GetClientUseCase getClientUseCase, GetStaffUseCase getStaffUseCase, GetAntennaUseCase getAntennaUseCase, GetPaymentUseCase getPaymentUseCase, ShimmerEffectAnimation shimmerEffectAnimation) {
        this.appExecutor = appExecutor;
        this.navigator = navigator;
        this.authorizationService = authorizationService;
        this.getClientUseCase = getClientUseCase;
        this.getStaffUseCase = getStaffUseCase;
        this.getAntennaUseCase = getAntennaUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
        this.shimmerEffectAnimation = shimmerEffectAnimation;
    }

    @FXML
    public void initialize() {

        Platform.runLater(() -> shimmerEffectAnimation.start(
                vboxClientCount,
                incomeView,
                vboxSellAll,
                vboxStaffCount,
                vboxAntennaActive
        ));

        AtomicReference<List<Payment>> paymentsRef = new AtomicReference<>();

        Task<DashDto> task = new Task<>() {

            @Override
            protected DashDto call() throws Exception {

                List<Payment> payments = getPaymentUseCase.getAll();
                paymentsRef.set(payments);

                return new DashDto(
                        getPaymentUseCase.getAllCount(),
                        getClientUseCase.getAll().size(),
                        getStaffUseCase.getAllStaff().size(),
                        getAntennaUseCase.getAll().size()
                );
            }
        };

        task.setOnSucceeded(event -> {

            DashDto dto = task.getValue();

            lblStaffCount.setText(String.valueOf(dto.staffCount()));
            lblAntennasActive.setText(String.valueOf(dto.antennasActive()));
            lblClientsCount.setText(String.valueOf(dto.clientCount()));
            lblSellAllMoney.setText(String.valueOf(dto.allMoney()));

            loadIncomeChart(paymentsRef.get());
            shimmerEffectAnimation.stop();
        });

        task.setOnFailed(event -> {
            shimmerEffectAnimation.stop();
            task.getException().printStackTrace();
        });

        appExecutor.execute(task);
    }

    private void loadIncomeChart(List<Payment> payments) {

        incomeChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        String[] months = {
                "Ene", "Feb", "Mar", "Abr",
                "May", "Jun", "Jul", "Ago",
                "Sep", "Oct", "Nov", "Dic"
        };

        BigDecimal[] monthlyIncome = new BigDecimal[12];

        for (int i = 0; i < 12; i++) {
            monthlyIncome[i] = BigDecimal.ZERO;
        }

        int currentYear = LocalDateTime.now().getYear();

        for (Payment payment : payments) {

            if (payment == null || !payment.isPaid()) {
                continue;
            }

            LocalDateTime date = payment.getDatePaidUser();

            if (date == null || date.getYear() != currentYear) {
                continue;
            }

            int monthIndex = date.getMonthValue() - 1;

            monthlyIncome[monthIndex] =
                    monthlyIncome[monthIndex].add(payment.getAmount());
        }

        for (int i = 0; i < 12; i++) {

            series.getData().add(
                    new XYChart.Data<>(
                            months[i],
                            monthlyIncome[i].doubleValue()
                    )
            );
        }

        incomeChart.getData().add(series);
    }

}

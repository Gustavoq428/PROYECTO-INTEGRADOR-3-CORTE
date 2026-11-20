package usta.gym.application.port.in;

import usta.gym.domain.model.IncomeReport;

import java.time.LocalDate;

// Puerto de entrada: reporte de ingresos por rango de fechas
public interface IncomeReportUseCase {

    IncomeReport report(LocalDate from, LocalDate to);
}

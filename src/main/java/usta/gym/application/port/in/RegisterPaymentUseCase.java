package usta.gym.application.port.in;

import usta.gym.domain.model.Payment;
import usta.gym.domain.model.PaymentMethod;
import usta.gym.domain.model.PaymentReceipt;

import java.math.BigDecimal;
import java.util.List;

// Puerto de entrada: registrar pagos y consultar comprobantes
public interface RegisterPaymentUseCase {

    Payment register(Long enrollmentId, BigDecimal amount, PaymentMethod method);

    List<Payment> listByEnrollment(Long enrollmentId);

    PaymentReceipt findReceipt(Long paymentId);
}

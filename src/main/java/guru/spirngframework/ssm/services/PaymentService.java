package guru.spirngframework.ssm.services;

import guru.spirngframework.ssm.domain.Payment;
import guru.spirngframework.ssm.domain.PaymentEvent;
import guru.spirngframework.ssm.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {
    Payment newPayment(Payment payment);

    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);


}

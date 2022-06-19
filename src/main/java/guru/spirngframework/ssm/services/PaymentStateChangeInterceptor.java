package guru.spirngframework.ssm.services;

import guru.spirngframework.ssm.domain.Payment;
import guru.spirngframework.ssm.domain.PaymentEvent;
import guru.spirngframework.ssm.domain.PaymentState;
import guru.spirngframework.ssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {
    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition, StateMachine<PaymentState, PaymentEvent> stateMachine, StateMachine<PaymentState, PaymentEvent> rootStateMachine) {
        Optional.ofNullable(message).ifPresent(msg->{
            Optional.ofNullable(Long.class.cast(msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID,-1L)))
                    .ifPresent(paymentId->{
                        Payment payment = paymentRepository.findById(paymentId).orElse(null);
                        payment.setState(state.getId());
                        paymentRepository.save(payment);
                    });
        });
    }
}

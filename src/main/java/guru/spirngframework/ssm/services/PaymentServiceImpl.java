package guru.spirngframework.ssm.services;

import guru.spirngframework.ssm.domain.Payment;
import guru.spirngframework.ssm.domain.PaymentEvent;
import guru.spirngframework.ssm.domain.PaymentState;
import guru.spirngframework.ssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    public static final String PAYMENT_ID = "payment_id";
    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState,PaymentEvent> stateMachineFactory;

    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;
    @Override
    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState,PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId,sm,PaymentEvent.PRE_AUTHORIZE);
        return sm;
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState,PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId,sm,PaymentEvent.AUTHORIZE);
        return sm;
    }

    @Deprecated
    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        StateMachine<PaymentState,PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId,sm,PaymentEvent.AUTH_DECLINED);
        return sm;
    }


    private void sendEvent(Long paymentId, StateMachine<PaymentState,PaymentEvent> sm, PaymentEvent event){
        Message<PaymentEvent> msg = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID,paymentId)
                .build();
        sm.sendEvent(Mono.just(msg))
                .doOnComplete(() -> {
                    System.out.println("Event handling complete"+sm.getState().toString());
                })
                .subscribe();
    }

    private StateMachine<PaymentState,PaymentEvent> build(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        StateMachine<PaymentState,PaymentEvent> sm = stateMachineFactory
                .getStateMachine(Long.toString(payment.getId()));
        sm.stopReactively().subscribe();
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma->{
                    sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                    sma.resetStateMachineReactively(
                            new DefaultStateMachineContext<>(payment.getState(),null,null,null)).subscribe();
                });
        sm.startReactively().subscribe();
        return sm;
    }
}

package guru.spirngframework.ssm.config;

import guru.spirngframework.ssm.domain.PaymentEvent;
import guru.spirngframework.ssm.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class StateMachineConfigTest {
    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    void testNewStateMachine() throws Exception{
        StateMachine<PaymentState,PaymentEvent> sm = factory.getStateMachine(UUID.randomUUID());
        sm.startReactively().subscribe();
        System.out.println(sm.getState().toString());
        Message<PaymentEvent> message = MessageBuilder.withPayload(PaymentEvent.PRE_AUTHORIZE).build();
        sm.sendEvent(Mono.just(message))
                .doOnComplete(() -> {
                    System.out.println("Event handling complete"+sm.getState().toString());
                })
                .subscribe();
        Message<PaymentEvent> message2 = MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED).build();
        sm.sendEvent(Mono.just(message2))
                .doOnComplete(() -> {
                    System.out.println("Event handling complete"+sm.getState().toString());
                })
                .subscribe();
        Thread.sleep(10000);

    }
}
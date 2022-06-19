package guru.spirngframework.ssm.services;

import guru.spirngframework.ssm.domain.Payment;
import guru.spirngframework.ssm.domain.PaymentEvent;
import guru.spirngframework.ssm.domain.PaymentState;
import guru.spirngframework.ssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class PaymentServiceImplTest {
    @Autowired
    PaymentService paymentService;
    @Autowired
    PaymentRepository paymentRepository;
    Payment payment;
    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.03")).build();
    }

    @Transactional
    @Test
    void preAuth() {
        Payment savedPayment = paymentService.newPayment(payment);
        System.out.println("Should be NEW");
        System.out.println(savedPayment.getState());

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());

        Payment preAuthPayment = paymentRepository.findById(savedPayment.getId()).orElse(null);
        System.out.println("Should be PRE_AUTH or PRE_AUTH_ERROR");
        System.out.println(sm.getState().getId());
        System.out.println(preAuthPayment);
    }
    @Transactional
    @RepeatedTest(10)
    void testAuth() {
        Payment savedPayment = paymentService.newPayment(payment);

        StateMachine<PaymentState, PaymentEvent> preAuthsm = paymentService.preAuth(savedPayment.getId());
        Payment preAuthPayment = paymentRepository.findById(savedPayment.getId()).orElse(null);
        if (preAuthsm.getState().getId()== PaymentState.PRE_AUTH){
            System.out.println("Payment is Pre Authorized");
            StateMachine<PaymentState, PaymentEvent> authsm = paymentService.authorizePayment(savedPayment.getId());
            System.out.println("Result of Auth:" + authsm.getState().getId());
        }else {
            System.out.println("Payment fail preAuth");
        }
    }

}
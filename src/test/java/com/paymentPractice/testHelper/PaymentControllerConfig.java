package com.paymentPractice.testHelper;

import com.paymentPractice.payment.controller.PaymentController;
import com.paymentPractice.payment.service.PaymentCardCheckService;
import com.paymentPractice.payment.service.PaymentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentControllerConfig {
    @Bean
    public PaymentController paymentController(PaymentService paymentService, PaymentCardCheckService paymentCardCheckService) {
        return new PaymentController(paymentService, paymentCardCheckService);
    }
}

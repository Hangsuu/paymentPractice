package com.paymentPractice.testHelper;

import com.paymentPractice.payment.controller.PaymentController;
import com.paymentPractice.payment.model.*;
import com.paymentPractice.payment.repository.AmountRepository;
import com.paymentPractice.payment.service.*;
import com.paymentPractice.payment.service.impl.PaymentCardCheckServiceImpl;
import com.paymentPractice.payment.service.impl.PaymentServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentControllerConfig {
//    @Bean
//    public PaymentController paymentController(PaymentService paymentService, PaymentCardCheckService paymentCardCheckService) {
//        return new PaymentController(paymentService, paymentCardCheckService);
//    }

    @Bean
    public PaymentService paymentService(AmountRepository amountRepository,
                                         CardInformationConversionService cardInformationConversionService,
                                         TransferAndGetStringDataService transferAndGetStringDataService,
                                         SavePaymentService savePaymentService) {
        return new PaymentServiceImpl(amountRepository, cardInformationConversionService, transferAndGetStringDataService, savePaymentService);
    }

    @Bean
    public PaymentCardCheckService paymentCardCheckService(PaymentService paymentService, CardNumberService cardNumberService) {
        return new PaymentCardCheckServiceImpl(paymentService, cardNumberService);
    }
}

package com.paymentPractice.payment.controller;

import com.paymentPractice.PaymentPracticeApplication;
import com.paymentPractice.payment.service.*;
import com.paymentPractice.testHelper.PaymentControllerConfig;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(PaymentController.class)
@ContextConfiguration(classes = {PaymentPracticeApplication.class, PaymentControllerConfig.class})
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PaymentService paymentService; // 필요한 의존성 주입
    @MockBean
    private PaymentCardCheckService paymentCardCheckService;

    @Test
    void paymentTest() throws Exception {
        String paymentJson = "{"
                + "\"cardNumber\": \"1234123412341234\","
                + "\"expirationPeriod\": \"1234\","
                + "\"cvc\": \"123\","
                + "\"installmentMonths\": \"12\","
                + "\"amount\": 11000,"
                + "\"vat\": 1000"
                + "}";
        mockMvc.perform(
                post("/rest/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson)
        ).andExpect(status().isOk())
        .andDo(print());
    }

    @Test
    void paymentCancellationTest() throws Exception {
        String paymentJson = "{"
                + "\"cardNumber\": \"1234123412341234\","
                + "\"expirationPeriod\": \"1234\","
                + "\"cvc\": \"123\","
                + "\"installmentMonths\": \"12\","
                + "\"amount\": 11000,"
                + "\"vat\": 1000"
                + "}";
        mockMvc.perform(
                post("/rest/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson)
        ).andExpect(status().isOk())
        .andDo(print());

        String cancellationJson = "{" +
                "\"amountId\": \"AM240320212655457350\"" +
                "}";
        mockMvc.perform(
                        post("/rest/paymentCancellation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cancellationJson)
                ).andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void paymentInformationTest() throws Exception {
        mockMvc.perform(get("/rest/paymentInformation")
                        .param("amountId", "AM240320212655457350"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void partialCancellationTest() throws Exception {
        String paymentJson = "{"
                + "\"cardNumber\": \"1234123412341234\","
                + "\"expirationPeriod\": \"1234\","
                + "\"cvc\": \"123\","
                + "\"installmentMonths\": \"12\","
                + "\"amount\": 11000,"
                + "\"vat\": 1000"
                + "}";
        mockMvc.perform(
                        post("/rest/payment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(paymentJson)
                ).andExpect(status().isOk())
                .andDo(print());

        String cancellationJson = "{" +
                "\"amountId\" : \"AM2403162339292629c7\"," +
                "\"amount\" : 90000," +
                "\"vat\" : null" +
                "}";
        mockMvc.perform(
                        post("/rest/partialCancellation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(cancellationJson)
                ).andExpect(status().isOk())
                .andDo(print());
    }
}

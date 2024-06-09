package com.paymentPractice.payment.controller;

import com.paymentPractice.payment.service.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc
public class PaymentControllerMVCTest {

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
        String cancellationJson = "{" +
                "\"amountId\": \"testAmountId\"" +
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
                        .param("amountId", "testAmountId"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void partialCancellationTest() throws Exception {
        String cancellationJson = "{" +
                "\"amountId\" : \"testAmountId\"," +
                "\"amount\" : 9000," +
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

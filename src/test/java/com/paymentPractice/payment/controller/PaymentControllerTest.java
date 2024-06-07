package com.paymentPractice.payment.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentPractice.PaymentPracticeApplication;
import com.paymentPractice.common.model.CommonResponseModel;
import com.paymentPractice.common.service.TwoWayEncryptionService;
import com.paymentPractice.payment.model.PaymentResultVO;
import com.paymentPractice.payment.model.PaymentSO;
import com.paymentPractice.payment.repository.AmountRepository;
import com.paymentPractice.payment.repository.CardNumberRepository;
import com.paymentPractice.payment.repository.PaymentRepository;
import com.paymentPractice.payment.repository.TransferDataRepository;
import com.paymentPractice.payment.service.*;
import com.paymentPractice.testHelper.PaymentControllerConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
//@RunWith(SpringRunner.class)
//@WebMvcTest(PaymentController.class)
//@ContextConfiguration(classes = {PaymentControllerConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PaymentService paymentService; // 필요한 의존성 주입
    @Autowired
    private PaymentCardCheckService paymentCardCheckService;
    @Autowired
    private AmountRepository amountRepository;
    @Autowired
    private CardInformationConversionService cardInformationConversionService;
    @Autowired
    private TransferAndGetStringDataService transferAndGetStringDataService;
    @Autowired
    private SavePaymentService savePaymentService;
    @Autowired
    private TwoWayEncryptionService twoWayEncryptionService;
    @Autowired
    private CardApiService cardApiService;
    @Autowired
    private TransferDataRepository transferDataRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private CardNumberService cardNumberService;
    @Autowired
    private CardNumberRepository cardNumberRepository;

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
        String amountId = getDefaultAmountId();

        String cancellationJson = "{" +
                "\"amountId\": \"" + amountId + "\"" +
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
        String amountId = getDefaultAmountId();

        mockMvc.perform(get("/rest/paymentInformation")
                        .param("amountId", amountId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void partialCancellationTest() throws Exception {
        String amountId = getDefaultAmountId();

        String cancellationJson = "{" +
                "\"amountId\" : \"" + amountId + "\"," +
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

    private String getDefaultAmountId() throws Exception {
        String paymentJson = "{"
                + "\"cardNumber\": \"1234123412341234\","
                + "\"expirationPeriod\": \"1234\","
                + "\"cvc\": \"123\","
                + "\"installmentMonths\": \"12\","
                + "\"amount\": 11000,"
                + "\"vat\": 1000"
                + "}";
        MvcResult mvcResult = mockMvc.perform(
                        post("/rest/payment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(paymentJson)
                ).andExpect(status().isOk())
                .andDo(print()).andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        CommonResponseModel paymentResponse = objectMapper.readValue(responseBody,
                new TypeReference<CommonResponseModel<PaymentResultVO>>() {
                });
        PaymentResultVO paymentResultVO = (PaymentResultVO) paymentResponse.getData();
        String amountId = paymentResultVO.getAmountId();

        log.info("payed amount Id : {}", amountId);
        return amountId;
    }
}

package com.paymentPractice.payment.controller;

import com.paymentPractice.common.model.CommonResponseModel;
import com.paymentPractice.payment.model.*;
import com.paymentPractice.payment.service.PaymentCardCheckService;
import com.paymentPractice.payment.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest")
@AllArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentCardCheckService paymentCardCheckService;

    @PostMapping("/payment")
    public ResponseEntity<CommonResponseModel> payment(@RequestBody PaymentSO paymentSO) {
        PaymentResultVO paymentResultVO = paymentCardCheckService.payment(paymentSO);
        return new ResponseEntity<>(new CommonResponseModel<>(paymentResultVO), HttpStatus.OK);
    }

    @PostMapping("/paymentCancellation")
    public ResponseEntity<CommonResponseModel> paymentCancellation(@RequestBody PaymentCancellationSO paymentCancellationSO) {
        PaymentResultVO paymentResultVO = paymentService.paymentCancellation(paymentCancellationSO);
        return new ResponseEntity<>(new CommonResponseModel<>(paymentResultVO), HttpStatus.OK);
    }

    @GetMapping("/paymentInformation")
    public ResponseEntity<CommonResponseModel> paymentInformation(@ModelAttribute PaymentInformationSO paymentInformationSO) {
        PaymentInformationVO paymentInformationVO = paymentService.paymentInformation(paymentInformationSO);
        return new ResponseEntity<>(new CommonResponseModel<>(paymentInformationVO), HttpStatus.OK);
    }

    @PostMapping("/partialCancellation")
    public ResponseEntity<CommonResponseModel> partialCancellation(@RequestBody PartialCancellationSO partialCancellationSO) {
        PaymentResultVO paymentResultVO = paymentService.partialCancellation(partialCancellationSO);
        return new ResponseEntity<>(new CommonResponseModel<>(paymentResultVO), HttpStatus.OK);
    }
}

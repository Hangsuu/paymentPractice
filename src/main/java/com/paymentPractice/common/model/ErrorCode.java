package com.paymentPractice.common.model;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 400
    WRONG_CARD_NUMBER(HttpStatus.BAD_REQUEST, "잘못된 형식의 카드번호입니다."),
    WRONG_EXPIRATION_PERIOD(HttpStatus.BAD_REQUEST, "잘못된 형식의 유효기간입니다."),
    WRONG_CVC(HttpStatus.BAD_REQUEST, "잘못된 형식의 CVC입니다."),
    WRONG_INSTALLMENT_MONTHS(HttpStatus.BAD_REQUEST, "잘못된 범위의 할부개월입니다."),
    WRONG_AMOUNT(HttpStatus.BAD_REQUEST, "잘못된 범위의 결제금액입니다."),
    WRONG_VAT(HttpStatus.BAD_REQUEST, "잘못된 범위의 부가가치세입니다."),
    WRONG_EXPIRATION_DATE(HttpStatus.BAD_REQUEST, "유효기간이 만료된 카드입니다."),
    WRONG_AMOUNT_ID(HttpStatus.BAD_REQUEST, "잘못된 id입니다."),
    ALREADY_CANCELLED_PAYMENT(HttpStatus.BAD_REQUEST, "이미 취소처리된 결제입니다."),
    EXCEED_REST_AMOUNT(HttpStatus.BAD_REQUEST, "취소 금액이 잔여 금액을 초과했습니다."),
    EXCEED_REST_VAT(HttpStatus.BAD_REQUEST, "취소 부가가치세가 잔여 부가가치세를 초과했습니다."),
    EXIST_REST_VAT(HttpStatus.BAD_REQUEST, "잔여 부가가치세가 존재합니다."),
    WRONG_PAYMENT(HttpStatus.BAD_REQUEST, "결제 정보를 찾을 수 없습니다."),

    // 500
    TRANSFER_DATA_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "카드정보 전송을 실패했습니다."),
    USING_CARD_NUMBER(HttpStatus.INTERNAL_SERVER_ERROR, "결제 진행중인 카드 정보입니다.");

    private final HttpStatus httpStatus;
    private final String detail;

    ErrorCode(HttpStatus httpStatus, String detail) {
        this.httpStatus = httpStatus;
        this.detail = detail;
    }
}

package com.paymentPractice.payment.model;

import com.paymentPractice.common.exception.CustomException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;

@Getter
@Setter
@Builder
public class DataSenderVO {
    // 카드 번호
    @StringLength(length = 20)
    private String cardNumber;

    // 할부 개월 수
    @StringLength(length = 2)
    private int installmentMonths;

    // 카드 유효 기간
    @StringLength(length = 4)
    private String expirationPeriod;

    // cvc
    @StringLength(length = 3)
    private int cvc;

    // 거래 금액
    @StringLength(length = 10)
    private int amount;

    // 부가가치세
    @StringLength(length = 10)
    private int vat;

    // 원거래 관리번호
    @StringLength(length = 20)
    private String originalManagementNumber;

    // 암호화된 카드 정보
    @StringLength(length = 300)
    private String encryptedCardInformation;

    // 예비 필드
    @StringLength(length = 47)
    private String spareField;

    // 데이터 전송용 String data 생성
    public String getStringData() {
        StringBuffer stringBuffer = new StringBuffer();
        // 카드 번호 (3  형태)
        stringBuffer.append(this.cardNumber);
        addSpace("cardNumber", this.cardNumber, " ", stringBuffer);

        // 할부 개월 수 (003 형태)
        addSpace("installmentMonths", String.valueOf(this.installmentMonths), "0", stringBuffer);
        stringBuffer.append(this.installmentMonths);

        // 카드 유효 기간 (3  형태)
        stringBuffer.append(this.expirationPeriod);
        addSpace("expirationPeriod", this.expirationPeriod, " ", stringBuffer);

        // cvc (3  형태)
        stringBuffer.append(this.cvc);
        addSpace("cvc", String.valueOf(this.cvc), " ", stringBuffer);

        // 거래 금액 (  3형태)
        addSpace("amount", String.valueOf(this.amount), " ", stringBuffer);
        stringBuffer.append(this.amount);

        // 부가가치세  (003 형태)
        addSpace("vat", String.valueOf(this.vat), "0", stringBuffer);
        stringBuffer.append(this.vat);

        // 원거래 관리번호 (ab 형태)
        stringBuffer.append(this.originalManagementNumber);
        addSpace("originalManagementNumber", this.originalManagementNumber, " ", stringBuffer);

        // 암호화된 카드 정보 (ab 형태)
        stringBuffer.append(this.encryptedCardInformation);
        addSpace("encryptedCardInformation", this.encryptedCardInformation, " ", stringBuffer);

        // 예비 필드 (ab 형태)
        stringBuffer.append(this.spareField);
        addSpace("spareField", this.spareField, " ", stringBuffer);

        return stringBuffer.toString();
    }

    // 공백 추가 메서드
    private void addSpace(String fieldName, String fieldData, String spaceType, StringBuffer stringBuffer) {
        int fieldDataLength = fieldData.length();
        int maxFieldDataLength = getLength(fieldName);
        if (fieldDataLength > maxFieldDataLength) {
            throw new CustomException(fieldName + " 필드의 길이가 초과됐습니다.", HttpStatus.BAD_REQUEST);
        }
        for(int i=0; i < maxFieldDataLength - fieldDataLength; i++) {
            stringBuffer.append(spaceType);
        }
    }

    // StrengthLength 어노테이션의 length값 반환
    private int getLength(String fieldName) {
        // 필드에 적용된 어노테이션 정보 정보
        try {
            Field field = DataSenderVO.class.getDeclaredField(fieldName);
            return field.getAnnotation(StringLength.class).length();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}

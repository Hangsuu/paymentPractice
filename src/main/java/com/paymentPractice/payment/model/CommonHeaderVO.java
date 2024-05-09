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
public class CommonHeaderVO {
    // 데이터 길이
    @StringLength(length = 4)
    private int dataLength;

    // 데이터 구분
    @StringLength(length = 10)
    private String dataDivision;
    
    // 관리번호
    @StringLength(length = 20)
    private String managementNumber;

    // 데이터 전송용 String data 생성
    public String getStringData() {
        StringBuffer stringBuffer = new StringBuffer();
        // 데이터길이 필드 공백 추가
        addSpace("dataLength", String.valueOf(this.dataLength), " ", stringBuffer);
        stringBuffer.append(this.dataLength);

        // 데이터구분 필드 공백 추가
        stringBuffer.append(this.dataDivision);
        addSpace("dataDivision", this.dataDivision, " ", stringBuffer);

        // 관리번호 필드 공백 추가
        stringBuffer.append(this.managementNumber);
        addSpace("managementNumber", this.managementNumber, " ", stringBuffer);
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
            Field field = CommonHeaderVO.class.getDeclaredField(fieldName);
            return field.getAnnotation(StringLength.class).length();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}

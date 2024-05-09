# 결제시스템
카드결제 / 결제취소 / 결제정보 조회 REST API

## 개발환경
- java 17
- Spring Boot 3.2.3
- Gradle 8.6
- H2 2.2.224

## 테이블 설계
- [ErdCloud](https://www.erdcloud.com/d/4suBNDzdCmfqG2EdJ)
![image](https://github.com/kakao-insurance-quiz/20240314-ohs/assets/121531812/ea5bc7ba-029b-4c09-9ff1-4fb301730ae6)
- 결제정보를 부모테이블, 금액정보를 자식테이블로 하여 정규화하였습니다.
  - 결제정보 테이블에는 유저정보, 카드정보, 전체결제상태, 할부개월수와 같은 결제에 대한 공통 데이터 저장하였습니다.
  - 결제테이블에 낙관적 락을 설정하여 같은 결제건에 대하여 동시에 취소처리가 되지 않도록 조치하였습니다.
  - 금액 테이블에는 결제금액, 결제금액 구분(결제, 취소), 부가가치세와 같은 결제, 취소 상태에 따라 달라지는 데이터 저장하였습니다.
- 카드사의 통신을 가정하여 전송 데이터 테이블 생성하였습니다.
- 동시에 같은 카드번호로 결제가 발생하는 것을 막기 위해 낙관적 락을 설정하고 카드번호를 pk로 갖고 있는 테이블 생성하였습니다.

## API
### [POST] /rest/payment - 카드결제
- 카드정보와 금액정보를 입력받아서 카드사에 전달할 String데이터를 저장하고 결제금액 ID와 함께 리턴됩니다.
  - 카드번호, 유효기간, 유효기간 만료여부, 금액 등 모든 데이터에 대해 유효성 체크 후 부적합 시 400 에러를 리턴합니다.
  - 카드정보는 카드번호/유효기간/cvc 로 결합 후 암호화 하여 저장합니다.
  - 동일한 카드번호로 결제가 동시 진행되지 않도록 결제 전 후에 카드번호 테이블에 데이터 저장, 삭제를 시행합니다.
```
{
    "cardNumber" : (String) 카드번호 ex)"1234123412341234",
    "expirationPeriod" : (String) 유효기간 ex)"12341234",
    "cvc" : (String) cvc ex)"777",
    "installmentMonths" : (int) 할부개월수 ex)12,
    "amount" : (int) 결제금액 ex)100000,
    "vat" : (Integer) 부가가치세 ex)null
}
```
### [POST] /rest/paymentCancellation - 결제취소
- 결제금액ID를 입력받아 전체 취소를 실행하고 전달된 String데이터와 관리번호를 리턴합니다.
  - 결제금액ID에 대한 데이터가 존재하는지 확인 후 미존재 시 400 에러를 리턴합니다.
  - 결제취소대상 금액과 부가가치세를 체크하여 저장합니다.
  - 결제상태가 바로 CANCELLATION으로 변환되고 더이상 결제취소를 할 수 없습니다.
```
{
    "amountId": (String) 결제금액ID ex)"AM240318230948687747"
}
```
### [GET] /rest/paymentInformation - 결제정보 조회
- 결제금액ID를 입력받아 카드정보, 금액, 부가가치세 등을 리턴합니다.
  - 결제금액ID에 대한 데이터가 존재하는지 확인 후 미존재 시 400 에러를 리턴합니다.
  - 결제테이블의 카드정보를 복호화한 후 카드번호는 마스킹 처리하여 리턴합니다.
```
/rest/paymentInformation?amountId=[결제금액ID]
```
### [POST] /rest/partialCancellation - 부분취소
- 결제금액ID와 취소금액, 부가가치세를 입력받아 부분 취소를 진행하고 전달된 String 데이터와 관리번호를 리턴합니다.
  - 부분취소 금액 유효성 체크, 유효한 결제금액 ID인지 체크하여 취소 가능 여부를 판단합니다.(불가 시 400에러)
  - 취소 후 잔여 결제금액에 따라 결제상태를 PARTAIL_CANCELLATION 또는 CANCELLATION으로 전환합니다.
```
{
    "amountId" : (String) 결제금액 ID ex)"AM240319202801625c88",
    "amount" : (int) 금액 ex)90000,
    "vat" : (Integer) 부가가치세 ex)null
}
```
### 응답데이터
- 카드결제, 결제취소, 부분취소
```
{
    "message": null,
    "data": {
        "amountId": "AM240319212901753353",
        "stringData": " 446PAYMENT   AM2403192129017533531234123412311234    121234123    1000000000009091                    908CxqrzrvzKSgfFdS7605XsJntYA0KKJJwNi2Jvb4Q=                                                                                                                                                                                                                                                                tempUserId                                     "
    },
    "extraData": {},
    "success": true
}
```
- 결제정보 조회
```
{
    "message": null,
    "data": {
        "amountId": "AM240319212227422314",
        "cardNumber": "123412*******234",
        "expirationPeriod": "1234",
        "cvc": "123",
        "amountType": "PAYMENT",
        "amount": 100000,
        "vat": 9091
    },
    "extraData": {},
    "returnCode": null,
    "success": true
}
```
- 에러
```
{
    "timestamp": "2024-03-19T21:28:42.5432939",
    "status": 400,
    "error": "BAD_REQUEST",
    "message": "잘못된 id입니다."
}
```

## 프로젝트 기능
### 어노테이션
- MethodLog(description = "default: method")
  - 주요 메서드에 적용 시 메서드 시작과 끝에 description + uuid + 메서드 로그가 찍힐 수 있도록 Aspect로 구현했습니다.
- StringLength(length = (int))
  - 카드사 전송데이터 작성을 시 String 길이를 입력하고 데이터를 변환하는데 사용되거나 전체 길이를 구할 때 사용됩니다.
### 인터셉터
- ControllerLogInterceptor
  - 모든 Rest Api 호출 시 호출 시작과 끝에 uuid + URI + handler 순으로 로그가 찍힐 수 있도록 구현했습니다.
### 예외처리
- CustomExceptionHandler
  - 공통예외객체를 반환하기 위해 예외 발생 시 ExceptionHandler를 통해 ErrorResponseModel 객체가 리턴되도록 구현했습니다.
### 결제, 결제금액 ID 생성
- [테이블코드(Enum)(2)][yyMMddHHmmssSSS(15)][UUID(3)] 형태로 채번됩니다.

## TODO
- 테이블 추가 설계 없이 카드번호 동시성 처리할 수 있는 방안 강구
  - ConcurrentSkipListSet, Reddison 락 기능을 시도해봤지만 결국 테이블로 해결되어 보완하여 성능개선이 필요하다 판단됩니다.
- 결제테이블에 결제상품 정보 등 설계 고려
- 단위테스트가 용이한 구조로 리팩터링

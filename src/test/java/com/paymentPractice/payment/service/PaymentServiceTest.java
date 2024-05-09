package com.paymentPractice.payment.service;

import com.paymentPractice.common.exception.CustomException;
import com.paymentPractice.common.service.TwoWayEncryptionService;
import com.paymentPractice.payment.entity.AmountType;
import com.paymentPractice.payment.model.*;
import com.paymentPractice.testHelper.TransactionalTestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
public class PaymentServiceTest {
    @Autowired
    PaymentService paymentService;
    @Autowired
    TwoWayEncryptionService twoWayEncryptionService;
    @Autowired
    TransactionalTestService transactionalTestService;
    @Autowired
    CardNumberService cardNumberService;

    private static String UUIDUserID = "";

    @BeforeAll
    static void setUUIDUserId() {
        UUIDUserID = UUID.randomUUID().toString().substring(0, 20);
    }

    @AfterEach
    void deleteData() {
        transactionalTestService.deleteByUserId(UUIDUserID);
    }

    @Test
    @DisplayName("결제 일반 테스트")
    void paymentTest() {
        log.info("PaymentServiceTest.paymentTest start");
        PaymentSO paymentSO = setBasicPaymentSO();

        paymentService.payment(paymentSO);

        assertThat(transactionalTestService.getPaymentNumberByUserId(UUIDUserID)).isEqualTo(1);
    }

    @Test
    @DisplayName("전체취소 동시성 테스트")
    void paymentCancellationTest() throws InterruptedException {
        log.info("PaymentServiceTest.paymentCancellationTest start");
        PaymentSO paymentSO = setBasicPaymentSO();

        PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);

        Runnable task = () -> {
            PaymentCancellationSO paymentCancellationSO = new PaymentCancellationSO();
            paymentCancellationSO.setAmountId(rightPaymentResult.getAmountId());
            paymentService.paymentCancellation(paymentCancellationSO);
        };

        // 여러 스레드에서 동시에 실행되도록 함
        int threadCount = 2;
        Thread[] threads = new Thread[threadCount];

        for (int i=0; i<threadCount; i++) {
            threads[i] = new Thread(task);
        }

        // 스레드 시작
        for (Thread thread : threads) {
            thread.start();
        }

        // 모든 스레드가 종료될 때까지 대기
        for (Thread thread : threads) {
            thread.join();
        }

        // 취소 n건 중 1건만 처리
        int cancelInformationNumber = transactionalTestService.getCancelInformationNumber(rightPaymentResult.getAmountId());
        assertThat(cancelInformationNumber).isEqualTo(1);
    }
    @Nested
    @DisplayName("케이스별 결제정보 반환 확인 테스트")
    class PaymentInformationTest {
        @Test
        @DisplayName("일반 결제정보 확인")
        void paymentResultInformationTest() {
            log.info("PaymentServiceTest.PaymentInformationTest.paymentResultInformationTest start");
            PaymentSO paymentSO = setBasicPaymentSO();
            PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);

            // 결제 정보 확인
            PaymentInformationSO paymentInformationSO = new PaymentInformationSO();
            paymentInformationSO.setAmountId(rightPaymentResult.getAmountId());

            PaymentInformationVO paymentInformation = paymentService.paymentInformation(paymentInformationSO);
            assertThat(paymentInformation.getCardNumber()).isEqualTo("123412*******234");
            assertThat(paymentInformation.getExpirationPeriod()).isEqualTo("0499");
            assertThat(paymentInformation.getCvc()).isEqualTo("777");
            assertThat(paymentInformation.getAmountId()).isEqualTo(rightPaymentResult.getAmountId());
            assertThat(paymentInformation.getAmountType()).isEqualTo(AmountType.PAYMENT);
        }
        @Test
        @DisplayName("취소 결제정보 확인")
        void cancellationResultInformationTest() {
            log.info("PaymentServiceTest.PaymentInformationTest.cancellationResultInformationTest start");
            PaymentSO paymentSO = setBasicPaymentSO();
            PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);

            // 결제 정보 확인
            PaymentInformationSO paymentInformationSO = new PaymentInformationSO();

            // 부분취소 확인
            PartialCancellationSO partialCancellationSO = new PartialCancellationSO();
            partialCancellationSO.setAmount(5000);
            partialCancellationSO.setVat(null);
            partialCancellationSO.setAmountId(rightPaymentResult.getAmountId());
            PaymentResultVO partialCancelResult = paymentService.partialCancellation(partialCancellationSO);

            paymentInformationSO.setAmountId(partialCancelResult.getAmountId());
            // 부분취소
            PaymentInformationVO partialPaymentInformation = paymentService.paymentInformation(paymentInformationSO);
            // 부분취소 반환객체 데이터 테스트
            assertThat(partialPaymentInformation.getCardNumber()).isEqualTo("123412*******234");
            assertThat(partialPaymentInformation.getExpirationPeriod()).isEqualTo("0499");
            assertThat(partialPaymentInformation.getCvc()).isEqualTo("777");
            assertThat(partialPaymentInformation.getAmountId()).isEqualTo(partialCancelResult.getAmountId());
            assertThat(partialPaymentInformation.getAmountType()).isEqualTo(AmountType.CANCEL);
        }
        @Test
        @DisplayName("부분취소 결제정보 확인")
        void partialCancellationResultInformationTest() {
            log.info("PaymentServiceTest.PaymentInformationTest.partialCancellationResultInformationTest start");
            PaymentSO paymentSO = setBasicPaymentSO();
            PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);

            // 결제 정보 확인
            PaymentInformationSO paymentInformationSO = new PaymentInformationSO();

            // 완전취소 확인
            PaymentCancellationSO paymentCancellationSO = new PaymentCancellationSO();
            paymentCancellationSO.setAmountId(rightPaymentResult.getAmountId());
            PaymentResultVO cancellationResult = paymentService.paymentCancellation(paymentCancellationSO);

            paymentInformationSO.setAmountId(cancellationResult.getAmountId());
            // 남은 금액 완전취소
            PaymentInformationVO cancelPaymentInformation = paymentService.paymentInformation(paymentInformationSO);
            // 완전취소 반환객체 데이터 테스트
            assertThat(cancelPaymentInformation.getCardNumber()).isEqualTo("123412*******234");
            assertThat(cancelPaymentInformation.getExpirationPeriod()).isEqualTo("0499");
            assertThat(cancelPaymentInformation.getCvc()).isEqualTo("777");
            assertThat(cancelPaymentInformation.getAmountId()).isEqualTo(cancellationResult.getAmountId());
            assertThat(cancelPaymentInformation.getAmountType()).isEqualTo(AmountType.CANCEL);
        }
    }

    @Test
    @DisplayName("부분취소 동시성 테스트")
    void partialCancellationTest() throws InterruptedException {
        log.info("PaymentServiceTest.partialCancellationTest start");
        PaymentSO paymentSO = setBasicPaymentSO();

        PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);

        Runnable task = () -> {
            PartialCancellationSO partialCancellationSO = new PartialCancellationSO();
            partialCancellationSO.setAmountId(rightPaymentResult.getAmountId());
            partialCancellationSO.setAmount(5000);
            paymentService.partialCancellation(partialCancellationSO);
        };

        // 여러 스레드에서 동시에 실행되도록 함
        int threadCount = 2;
        Thread[] threads = new Thread[threadCount];

        for (int i=0; i<threadCount; i++) {
            threads[i] = new Thread(task);
        }

        // 스레드 시작
        for (Thread thread : threads) {
            thread.start();
        }

        // 모든 스레드가 종료될 때까지 대기
        for (Thread thread : threads) {
            thread.join();
        }
        // 취소 2건 중 1건만 처리
        int cancelInformationNumber = transactionalTestService.getCancelInformationNumber(rightPaymentResult.getAmountId());
        assertThat(cancelInformationNumber).isEqualTo(1);
    }
    @Nested
    @DisplayName("부분취소 과제명세서 테스트케이스 테스트")
    class PartialCancellationTest {
        @Test
        @DisplayName("과제명세서 TestCase1")
        void partialCancellationTestCase1() {
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase1 start");
            // 결제(금액 11000, 부가가치세 1000) 성공
            PaymentSO paymentSO = setBasicPaymentSO();
            paymentSO.setAmount(11000);
            paymentSO.setVat(1000);

            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase1 first payment");
            PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);

            // 부분취소(금액 1100, 부가가치세 100) 성공
            PartialCancellationSO partialCancellationSO1 = new PartialCancellationSO();
            partialCancellationSO1.setAmount(1100);
            partialCancellationSO1.setVat(100);
            partialCancellationSO1.setAmountId(rightPaymentResult.getAmountId());
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase1 second partial cancel");
            paymentService.partialCancellation(partialCancellationSO1);

            // 부분취소(금액 3300, 부가가치세 null) 성공
            PartialCancellationSO partialCancellationSO2 = new PartialCancellationSO();
            partialCancellationSO2.setAmount(3300);
            partialCancellationSO2.setVat(null);
            partialCancellationSO2.setAmountId(rightPaymentResult.getAmountId());
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase1 third partial cancel");
            paymentService.partialCancellation(partialCancellationSO2);

            // 부분취소(금액 7000, 부가가치세 null) 실패
            PartialCancellationSO partialCancellationSO3 = new PartialCancellationSO();
            partialCancellationSO3.setAmount(7000);
            partialCancellationSO3.setVat(null);
            partialCancellationSO3.setAmountId(rightPaymentResult.getAmountId());
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase1 fourth partial cancel -> fail");
            assertThatThrownBy(() -> {
                paymentService.partialCancellation(partialCancellationSO3);
            }).isInstanceOf(CustomException.class);

            // 부분취소(금액 6600, 부가가치세 700) 실패
            PartialCancellationSO partialCancellationSO4 = new PartialCancellationSO();
            partialCancellationSO4.setAmount(6600);
            partialCancellationSO4.setVat(700);
            partialCancellationSO4.setAmountId(rightPaymentResult.getAmountId());
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase1 fifth partial cancel -> fail");
            assertThatThrownBy(() -> {
                paymentService.partialCancellation(partialCancellationSO4);
            }).isInstanceOf(CustomException.class);

            // 부분취소(금액 6600, 부가가치세 600) 성공
            PartialCancellationSO partialCancellationSO5 = new PartialCancellationSO();
            partialCancellationSO5.setAmount(6600);
            partialCancellationSO5.setVat(600);
            partialCancellationSO5.setAmountId(rightPaymentResult.getAmountId());
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase1 sixth partial cancel");
            paymentService.partialCancellation(partialCancellationSO5);

            // 부분취소(금액 100, 부가가치세 null) 실패
            PartialCancellationSO partialCancellationSO6 = new PartialCancellationSO();
            partialCancellationSO6.setAmount(100);
            partialCancellationSO6.setVat(null);
            partialCancellationSO6.setAmountId(rightPaymentResult.getAmountId());
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase1 seventh partial cancel");
            assertThatThrownBy(() -> {
                paymentService.partialCancellation(partialCancellationSO6);
            }).isInstanceOf(CustomException.class);
        }
        @Test
        @DisplayName("과제명세서 TestCase2")
        void partialCancellationTestCase2() {
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase1 start");
            // 결제(금액 20000, 부가가치세 909) 성공
            PaymentSO paymentSO = setBasicPaymentSO();
            paymentSO.setAmount(20000);
            paymentSO.setVat(909);

            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase2 first payment");
            PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);

            // 부분취소(금액 10000, 부가가치세 0) 성공
            PartialCancellationSO partialCancellationSO1 = new PartialCancellationSO();
            partialCancellationSO1.setAmount(10000);
            partialCancellationSO1.setVat(0);
            partialCancellationSO1.setAmountId(rightPaymentResult.getAmountId());
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase2 second partial cancel");
            paymentService.partialCancellation(partialCancellationSO1);

            // 부분취소(금액 10000, 부가가치세 0) 실패
            PartialCancellationSO partialCancellationSO2 = new PartialCancellationSO();
            partialCancellationSO2.setAmount(10000);
            partialCancellationSO2.setVat(0);
            partialCancellationSO2.setAmountId(rightPaymentResult.getAmountId());
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase2 third partial cancel -> fail");
            assertThatThrownBy(() -> {
                paymentService.partialCancellation(partialCancellationSO2);
            }).isInstanceOf(CustomException.class);

            // 부분취소(금액 10000, 부가가치세 9090) 성공
            PartialCancellationSO partialCancellationSO3 = new PartialCancellationSO();
            partialCancellationSO3.setAmount(10000);
            partialCancellationSO3.setVat(909);
            partialCancellationSO3.setAmountId(rightPaymentResult.getAmountId());
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase2 fourth partial cancel");
            paymentService.partialCancellation(partialCancellationSO3);
        }
        @Test
        @DisplayName("과제명세서 TestCase3")
        void partialCancellationTestCase3() {
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase1 start");
            // 결제(금액 20000, 부가가치세 null) 성공
            PaymentSO paymentSO = setBasicPaymentSO();
            paymentSO.setAmount(20000);
            paymentSO.setVat(null);

            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase3 first payment");
            PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);

            // 부분취소(금액 10000, 부가가치세 1000) 성공
            PartialCancellationSO partialCancellationSO1 = new PartialCancellationSO();
            partialCancellationSO1.setAmount(10000);
            partialCancellationSO1.setVat(1000);
            partialCancellationSO1.setAmountId(rightPaymentResult.getAmountId());
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase3 second partial cancel");
            paymentService.partialCancellation(partialCancellationSO1);

            // 부분취소(금액 10000, 부가가치세 909) 실패
            PartialCancellationSO partialCancellationSO2 = new PartialCancellationSO();
            partialCancellationSO2.setAmount(10000);
            partialCancellationSO2.setVat(909);
            partialCancellationSO2.setAmountId(rightPaymentResult.getAmountId());
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase3 third partial cancel -> fail");
            assertThatThrownBy(() -> {
                paymentService.partialCancellation(partialCancellationSO2);
            }).isInstanceOf(CustomException.class);

            // 부분취소(금액 10000, 부가가치세 null) 성공
            PartialCancellationSO partialCancellationSO3 = new PartialCancellationSO();
            partialCancellationSO3.setAmount(10000);
            partialCancellationSO3.setVat(null);
            partialCancellationSO3.setAmountId(rightPaymentResult.getAmountId());
            log.info("PaymentServiceTest.PartialCancellationTest.partialCancellationTestCase3 fourth partial cancel");
            paymentService.partialCancellation(partialCancellationSO3);
        }
    }

    @Nested
    @DisplayName("결제 진행 시 데이터 유효성 체크 적합성 테스트")
    class PaymentValidationCheckTest {
        @Test
        @DisplayName("카드번호 형식 오류")
        void cardNumberValidationCheckTest() {
            log.info("PaymentServiceTest.PaymentValidationCheckTest.cardNumberValidationCheckTest start");
            PaymentSO paymentSO = setBasicPaymentSO();

            paymentSO.setCardNumber("12341213412341234");
            assertThatThrownBy(() -> {paymentService.payment(paymentSO);}).isInstanceOf(CustomException.class);
        }
        @Test
        @DisplayName("유효하지 않은 만료일자 오류")
        void expirationPeriodValidationCheckTest() {
            log.info("PaymentServiceTest.PaymentValidationCheckTest.expirationPeriodValidationCheckTest start");
            PaymentSO paymentSO = setBasicPaymentSO();

            paymentSO.setExpirationPeriod("0224");
            assertThatThrownBy(() -> {paymentService.payment(paymentSO);}).isInstanceOf(CustomException.class);
        }
        @Test
        @DisplayName("만료일자 형식 오류")
        void expirationPeriodFormatValidationCheckTest() {
            log.info("PaymentServiceTest.PaymentValidationCheckTest.expirationPeriodFormatValidationCheckTest start");
            PaymentSO paymentSO = setBasicPaymentSO();

            paymentSO.setExpirationPeriod("032a");
            assertThatThrownBy(() -> {paymentService.payment(paymentSO);}).isInstanceOf(CustomException.class);
        }
        @Test
        @DisplayName("cvc 형식 오류")
        void cvcValidationCheckTest() {
            log.info("PaymentServiceTest.PaymentValidationCheckTest.cvcValidationCheckTest start");
            PaymentSO paymentSO = setBasicPaymentSO();

            paymentSO.setCvc("12a");
            assertThatThrownBy(() -> {paymentService.payment(paymentSO);}).isInstanceOf(CustomException.class);
        }
        @Test
        @DisplayName("할부개월 형식 오류")
        void installmentMonthsValidationCheckTest() {
            log.info("PaymentServiceTest.PaymentValidationCheckTest.installmentMonthsValidationCheckTest start");
            PaymentSO paymentSO = setBasicPaymentSO();

            paymentSO.setInstallmentMonths(-1);
            assertThatThrownBy(() -> {paymentService.payment(paymentSO);}).isInstanceOf(CustomException.class);
        }
        @Test
        @DisplayName("결제금액 범위 오류")
        void amountValidationCheckTest() {
            log.info("PaymentServiceTest.PaymentValidationCheckTest.amountValidationCheckTest start");
            PaymentSO paymentSO = setBasicPaymentSO();

            paymentSO.setAmount(99);
            assertThatThrownBy(() -> {paymentService.payment(paymentSO);}).isInstanceOf(CustomException.class);
        }
        @Test
        @DisplayName("부가가치세 범위 오류")
        void vatValidationCheckTest() {
            log.info("PaymentServiceTest.PaymentValidationCheckTest.vatValidationCheckTest start");
            PaymentSO paymentSO = setBasicPaymentSO();

            paymentSO.setAmount(100);
            paymentSO.setVat(101);
            assertThatThrownBy(() -> {paymentService.payment(paymentSO);}).isInstanceOf(CustomException.class);
        }
    }

    @Test
    @DisplayName("카드정보 암호화 테스트")
    void getEncryptedCardInformationTest() {
        log.info("PaymentServiceTest.getEncryptedCardInformationTest start");
        PaymentSO paymentSO = setBasicPaymentSO();

        PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);
        // return data에서 암호화 데이터 추출
        String encryptedData = rightPaymentResult.getStringData()
                .substring(rightPaymentResult.getStringData().length()-347, rightPaymentResult.getStringData().length()-47)
                .trim();
        // 카드번호/유효기간/cvc 형태 문자열을 옳게 암호화했는지 확인
        assertThat(encryptedData).isEqualTo(twoWayEncryptionService.encrypt("1234123412341234/0499/777"));
    }

    @Test
    @DisplayName("부가가치세 계산 테스트")
    void getCalculatedVatTest() {
        log.info("PaymentServiceTest.getCalculatedVatTest start");
        PaymentSO paymentSO = setBasicPaymentSO();

        // null로 설정한 vat값 반환 (/11)
        PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);

        PaymentInformationSO paymentInformationSO = new PaymentInformationSO();
        paymentInformationSO.setAmountId(rightPaymentResult.getAmountId());
        PaymentInformationVO paymentInformationVO = paymentService.paymentInformation(paymentInformationSO);
        assertThat(paymentInformationVO.getVat()).isEqualTo(9091);

        // 직접 설정한 vat 값 반환
        paymentSO.setVat(1000);
        PaymentResultVO rightPaymentResult2 = paymentService.payment(paymentSO);

        PaymentInformationSO paymentInformationSO2 = new PaymentInformationSO();
        paymentInformationSO.setAmountId(rightPaymentResult2.getAmountId());
        PaymentInformationVO paymentInformationVO2 = paymentService.paymentInformation(paymentInformationSO);
        assertThat(paymentInformationVO2.getVat()).isEqualTo(1000);

        // 반올림 0.5 미만 확인
        paymentSO.setAmount(11001);
        paymentSO.setVat(null);
        PaymentResultVO rightPaymentResult3 = paymentService.payment(paymentSO);

        PaymentInformationSO paymentInformationSO3 = new PaymentInformationSO();
        paymentInformationSO.setAmountId(rightPaymentResult3.getAmountId());
        PaymentInformationVO paymentInformationVO3 = paymentService.paymentInformation(paymentInformationSO);
        assertThat(paymentInformationVO3.getVat()).isEqualTo(1000);

    }

    @Test
    @DisplayName("String data 생성 및 전송 테스트")
    void getAndSendStringDataTest() {
        log.info("PaymentServiceTest.getAndSendStringDataTest start");
        PaymentSO paymentSO = setBasicPaymentSO();

        PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);
        // 카드번호 포함되어있는지 확인
        assertThat(rightPaymentResult.getStringData().contains("1234123412341234")).isTrue();
        // 만료기간이 포함되어있는지 확인
        assertThat(rightPaymentResult.getStringData().contains("0499")).isTrue();
        // cvc가 포함되어있는지 확인
        assertThat(rightPaymentResult.getStringData().contains("777")).isTrue();
        // 할부개월이 포함되어있는지 확인
        assertThat(rightPaymentResult.getStringData().contains("03")).isTrue();
        // 금액이 포함되어있는지 확인
        assertThat(rightPaymentResult.getStringData().contains("100000")).isTrue();
        // 부가가치세가 포함되어있는지 확인
        assertThat(rightPaymentResult.getStringData().contains("9091")).isTrue();
        // 카드정보 암호화가 포함되어있는지 확인
        assertThat(rightPaymentResult.getStringData().contains(twoWayEncryptionService.encrypt("1234123412341234/0499/777"))).isTrue();
    }

    @Test
    @DisplayName("올바른 amount id인지 판단 및 예외처리 테스트")
    void getAmountEntityByIdTest() {
        PaymentInformationSO paymentInformationSO = new PaymentInformationSO();
        paymentInformationSO.setAmountId("임의로 만든 가짜id");

        // 가짜 id로 데이터 조회 불가 예외
        assertThatThrownBy(() -> {
            paymentService.paymentInformation(paymentInformationSO);
        }).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("해당 결제 정보 반환 테스트")
    void getPaymentEntityTest() {
        log.info("PaymentServiceTest.getPaymentEntityTest start");
        PaymentSO paymentSO = setBasicPaymentSO();

        PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);
        PaymentCancellationSO paymentCancellationSO = new PaymentCancellationSO();
        paymentCancellationSO.setAmountId(rightPaymentResult.getAmountId());
        paymentService.paymentCancellation(paymentCancellationSO);

        // 한번 더 취소 시 예외발생
        assertThatThrownBy(() -> {
            paymentService.paymentCancellation(paymentCancellationSO);
        }).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("남은 결제금액과 부가가치세 계산 테스트")
    void getRestAmountAndVatTest() {
        log.info("PaymentServiceTest.getRestAmountAndVatTest start");
        PaymentSO paymentSO = setBasicPaymentSO();

        // 결제 데이터에서 금액, 부가가치세 추출
        PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);
        String amountString = rightPaymentResult.getStringData().substring(rightPaymentResult.getStringData().length()-387, rightPaymentResult.getStringData().length()-377).trim();
        String vatString = rightPaymentResult.getStringData().substring(rightPaymentResult.getStringData().length()-377, rightPaymentResult.getStringData().length()-367).trim();
        log.info("payment amount : {}, payment vat : {}", amountString, vatString);

        // 취소처리
        PaymentCancellationSO paymentCancellationSO = new PaymentCancellationSO();
        paymentCancellationSO.setAmountId(rightPaymentResult.getAmountId());
        PaymentResultVO cancelResultVO = paymentService.paymentCancellation(paymentCancellationSO);
        
        // 취소 데이터에서 금액, 부가가치세 추출
        String cancelAmountString = cancelResultVO.getStringData().substring(cancelResultVO.getStringData().length()-387, cancelResultVO.getStringData().length()-377).trim();
        String cancelVatString = cancelResultVO.getStringData().substring(cancelResultVO.getStringData().length()-377, cancelResultVO.getStringData().length()-367).trim();
        log.info("cancel payment amount : {}, cancel payment vat : {}", cancelAmountString, cancelVatString);

        // 결제금액과 취소금액 비교
        assertThat(amountString).isEqualTo(cancelAmountString);
        // 결제 vat와 취소 vat 비교
        assertThat(vatString).isEqualTo(cancelVatString);
    }

    @Test
    @DisplayName("카드정보 복호화 (카드번호/만료일자/cvc) 테스트")
    void getCardInformationTest() {
        log.info("PaymentServiceTest.getCardInformationTest start");
        PaymentSO paymentSO = setBasicPaymentSO();

        PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);

        PaymentInformationSO paymentInformationSO = new PaymentInformationSO();
        paymentInformationSO.setAmountId(rightPaymentResult.getAmountId());
        PaymentInformationVO paymentInformationVO = paymentService.paymentInformation(paymentInformationSO);

        String encryptedData = rightPaymentResult.getStringData()
                .substring(rightPaymentResult.getStringData().length()-347, rightPaymentResult.getStringData().length()-47)
                .trim();
        // 직접 복호화한 데이터와 반환받은 데이터 일치하는지 확인
        String[] decryptedDatas = twoWayEncryptionService.decrypt(encryptedData).split("/");
        String maskingCardNumber = "123412*******234";
        // 마스킹 카드번호와 결과 반환 카드번호 일치여부 비교
        assertThat(maskingCardNumber).isEqualTo(paymentInformationVO.getCardNumber());
        // 원래 카드번호와 복호화 데이터 비교
        assertThat("1234123412341234").isEqualTo(decryptedDatas[0]);
        // 정보조회 만료일자와 복호화 데이터 비교
        assertThat(paymentInformationVO.getExpirationPeriod()).isEqualTo(decryptedDatas[1]);
        // 정보조회 cvc와 복호화 데이터 비교
        assertThat(paymentInformationVO.getCvc()).isEqualTo(decryptedDatas[2]);
    }

    @Test
    @DisplayName("부분취소 유효성 체크 테스트")
    void partialCancellationValidationCheckTest() {
        log.info("PaymentServiceTest.partialCancellationValidationCheckTest start");
        PaymentSO paymentSO = setBasicPaymentSO();

        // 결제금액 범위 오류
        paymentSO.setInstallmentMonths(0);
        paymentSO.setAmount(99);
        assertThatThrownBy(() -> {paymentService.payment(paymentSO);}).isInstanceOf(CustomException.class);
        // 부가가치세 범위 오류
        paymentSO.setAmount(100);
        paymentSO.setVat(101);
        assertThatThrownBy(() -> {paymentService.payment(paymentSO);}).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("취소 가능 여부 판단 테스트")
    void partialCancellationAvailableCheckTest() {
        log.info("PaymentServiceTest.partialCancellationAvailableCheckTest start");
        PaymentSO paymentSO = setBasicPaymentSO();

        PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);

        PartialCancellationSO partialCancellationSO = new PartialCancellationSO();

        // 원래 금액보다 큰 금액 취소 시 예외
        partialCancellationSO.setAmount(1000000);
        partialCancellationSO.setVat(null);
        partialCancellationSO.setAmountId(rightPaymentResult.getAmountId());
        assertThatThrownBy(() -> {
            paymentService.partialCancellation(partialCancellationSO);
        }).isInstanceOf(CustomException.class);

        // 원래 금액보다 큰 부가가치세 취소 시 예외
        partialCancellationSO.setAmount(5000);
        partialCancellationSO.setVat(10000);
        assertThatThrownBy(() -> {
            paymentService.partialCancellation(partialCancellationSO);
        }).isInstanceOf(CustomException.class);

        // 잔여 부가가치세가 남는 경우 예외
        partialCancellationSO.setAmount(100000);
        partialCancellationSO.setVat(7000);
        assertThatThrownBy(() -> {
            paymentService.partialCancellation(partialCancellationSO);
        }).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("최초 결제 ID 테스트")
    void getFirstPaymentAmountIdTest() {
        log.info("PaymentServiceTest.getFirstPaymentAmountIdTest start");
        PaymentSO paymentSO = setBasicPaymentSO();

        PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);
        PaymentCancellationSO paymentCancellationSO = new PaymentCancellationSO();
        paymentCancellationSO.setAmountId(rightPaymentResult.getAmountId());

        PaymentResultVO cancellationPaymentResult = paymentService.paymentCancellation(paymentCancellationSO);

        // string data 정보에 결제 id 첨부되어 있는지 확인
        assertThat(cancellationPaymentResult.getStringData().contains(rightPaymentResult.getAmountId())).isTrue();
    }

    @Test
    @DisplayName("StrengthLength 어노테이션의 length값 총합 반환 테스트")
    void getHeaderAndDataLengthTest() {
        log.info("PaymentServiceTest.getHeaderAndDataLengthTest start");
        PaymentSO paymentSO = setBasicPaymentSO();

        PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);
        // 전체 length(446)이 우측정렬 되어 반환되는지 확인
        assertThat(rightPaymentResult.getStringData().startsWith(" 446")).isTrue();
    }

    @Test
    @DisplayName("완전 취소 시 할부개월 0으로 설정되는지 테스트")
    void changeInstallmentMonths() {
        log.info("PaymentServiceTest.changeInstallmentMonths start");
        PaymentSO paymentSO = setBasicPaymentSO();

        PaymentResultVO rightPaymentResult = paymentService.payment(paymentSO);

        // 전송 string data에서 할부개월수 추출
        String installmentMonthsString = rightPaymentResult.getStringData()
                .substring(54, 56)
                .trim();
        assertThat(installmentMonthsString).isEqualTo("03");

        // 부분취소 상태(바뀌지 않음)
        PartialCancellationSO partialCancellationSO = new PartialCancellationSO();
        partialCancellationSO.setAmount(5000);
        partialCancellationSO.setVat(null);
        partialCancellationSO.setAmountId(rightPaymentResult.getAmountId());
        PaymentResultVO partialCancelResult = paymentService.partialCancellation(partialCancellationSO);

        String partialCancelInstallmentMonthsString = partialCancelResult.getStringData()
                .substring(54, 56)
                .trim();
        // 바뀌지 않아 03으로 (3개월) 반환
        assertThat(partialCancelInstallmentMonthsString).isEqualTo("03");

        // 완전취소(00으로 설정)
        PaymentCancellationSO paymentCancellationSO = new PaymentCancellationSO();
        paymentCancellationSO.setAmountId(rightPaymentResult.getAmountId());
        PaymentResultVO cancellationResult = paymentService.paymentCancellation(paymentCancellationSO);

        String cancelInstallmentMonthsString = cancellationResult.getStringData()
                .substring(54, 56)
                .trim();
        // 바뀌어 00으로 반환
        assertThat(cancelInstallmentMonthsString).isEqualTo("00");
    }

    private PaymentSO setBasicPaymentSO() {
        PaymentSO paymentSO = new PaymentSO();
        paymentSO.setCardNumber("1234123412341234");
        paymentSO.setExpirationPeriod("0499");
        paymentSO.setCvc("777");
        paymentSO.setInstallmentMonths(3);
        paymentSO.setAmount(100000);
        paymentSO.setVat(null);
        paymentSO.setUserId(UUIDUserID);
        return paymentSO;
    }
}
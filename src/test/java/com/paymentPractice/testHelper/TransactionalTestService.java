package com.paymentPractice.testHelper;

import com.paymentPractice.payment.entity.AmountEntity;
import com.paymentPractice.payment.entity.AmountType;
import com.paymentPractice.payment.entity.PaymentEntity;
import com.paymentPractice.payment.repository.AmountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class TransactionalTestService {
    @Autowired
    PaymentTestRepository paymentRepository;
    @Autowired
    AmountRepository amountRepository;
    @Autowired
    TransferDataTestRepository transferDataRepository;

    @Transactional
    public void deleteByUserId(String uuidUserID) {
        paymentRepository.flush();
        amountRepository.flush();
        transferDataRepository.flush();
        paymentRepository.deleteByUserId(uuidUserID);
        transferDataRepository.deleteTestData(uuidUserID);
    }

    @Transactional
    public int getCancelInformationNumber(String amountId) {
        Optional<AmountEntity> findById = amountRepository.findById(amountId);
        if(findById.isEmpty()) {
            return 0;
        }
        AmountEntity amountEntity = findById.get();

        PaymentEntity paymentEntity = amountEntity.getPaymentEntity();

        List<AmountEntity> amounts = paymentEntity.getAmounts();

        return (int) amounts.stream().filter(amount -> amount.getAmountType() == AmountType.CANCEL).count();
    }

    @Transactional
    public int getPaymentNumberByUserId(String uuidUserId) {
        return paymentRepository.findAllByUserId(uuidUserId).size();
    }
}

package com.loyaltyplant.services;

import com.loyaltyplant.entity.Payment;
import com.loyaltyplant.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class PaymentService {

    @Autowired
    PaymentRepository paymentRepository;

    public BigDecimal getBalance(Long id) {
        BigDecimal balance = BigDecimal.ZERO;
        for(Payment payment : paymentRepository.findByToAccount(id)) {
            balance = balance.add(payment.getSum());
        }
        return balance;
    }

    /**
     * Метод предназначен для добавления, вычитания суммы со счета,
     * а также для перевода с одного счета в другой
     * @param payment
     * @return
     */
    @Transactional
    public ResponseEntity<String> transfer(Payment payment) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/html; charset=utf-8");
        BigDecimal balance = getBalance(payment.getToAccount());
        if(payment.getFromAccount() != null) {
            BigDecimal balanceFrom = getBalance(payment.getFromAccount());
            if(balanceFrom.compareTo(payment.getSum()) >= 0) {
                //если на счете отправителя достаточно средств, то списываем
                Payment from = new Payment();
                from.setDate(new Date());
                from.setSum(payment.getSum().negate());
                from.setToAccount(payment.getFromAccount());
                paymentRepository.save(from);
            } else {
                return new ResponseEntity("Недостаточно средств на счете-отправителя!", responseHeaders, HttpStatus.BAD_REQUEST);
            }
        }
        //Если сумма отрицительная, т.е. списываем, то проверяем на наличие средств
        if(payment.getSum().add(balance).compareTo(BigDecimal.ZERO) < 0) {
            return new ResponseEntity("Недостаточно средств для списания!", responseHeaders, HttpStatus.BAD_REQUEST);
        }

        paymentRepository.save(payment);
        return new ResponseEntity("", HttpStatus.OK);
    }
}

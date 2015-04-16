package com.loyaltyplant.controllers;

import com.loyaltyplant.entity.Payment;
import com.loyaltyplant.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @RequestMapping(value = "/balance/{id}", method = RequestMethod.GET)
    @ResponseBody
    public double getBalance(@PathVariable Long id) {
        return paymentService.getBalance(id).doubleValue();
    }

    @RequestMapping(value = "transfer", method = RequestMethod.POST)
    public ResponseEntity<String> transfer(@RequestParam(value = "from", required = false) Long from,
                                           @RequestParam("to") Long to,
                                           @RequestParam("amount") BigDecimal amount) {
        Payment payment = new Payment();
        payment.setDate(new Date());
        payment.setSum(amount);
        payment.setToAccount(to);
        payment.setFromAccount(from);
        return paymentService.transfer(payment);
    }
}

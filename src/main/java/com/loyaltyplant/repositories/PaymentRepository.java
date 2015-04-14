package com.loyaltyplant.repositories;

import com.loyaltyplant.entity.Payment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PaymentRepository extends CrudRepository<Payment, Long> {

    List<Payment> findByToAccount(Long id);
}

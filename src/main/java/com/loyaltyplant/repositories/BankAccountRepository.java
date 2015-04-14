package com.loyaltyplant.repositories;

import com.loyaltyplant.entity.BankAccount;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BankAccountRepository extends PagingAndSortingRepository<BankAccount, Long> {
}

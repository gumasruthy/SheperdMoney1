package com.shepherdmoney.interviewproject.repository;

import com.shepherdmoney.interviewproject.model.CreditCard;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Crud repository to store credit cards
 */
@Repository("CreditCardRepo")
public interface CreditCardRepository extends JpaRepository<CreditCard, Integer> {

    public Optional<CreditCard> findByNumber(String number);

}

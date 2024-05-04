package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

@RestController
public class CreditCardController {

    // Done: wire in CreditCard repository here (~1 line)
    @Autowired
    private UserRepository UserRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // Done: Create a credit card entity, and then associate that credit card with
        // user with given userId
        // Return 200 OK with the credit card id if the user exists and credit card is
        // successfully associated with the user
        // Return other appropriate response code for other exception cases
        // Do not worry about validating the card number, assume card number could be
        // any arbitrary format and length

        User user = UserRepository.findById(payload.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        CreditCard creditCard = new CreditCard();
        creditCard.setNumber(payload.getCardNumber());
        creditCard.setIssuanceBank(payload.getCardIssuanceBank());
        creditCard.setOwner(user);
        creditCard = creditCardRepository.save(creditCard); // Save the credit card

        if (creditCard != null) {
            return ResponseEntity.ok(creditCard.getId()); // Return the credit card ID
        } else {
            return ResponseEntity.internalServerError().build(); // Handle save outor
        }
    }

    @GetMapping("/credit-card/all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        // Done: return a list of all credit card associated with the given userId,
        // using CreditCardView class
        // if the user has no credit card, return empty list, never return null
        List<CreditCardView> returnValue = new ArrayList<>();
        User user = UserRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        } else {
            user.getCreditCards()
                    .forEach(cc -> returnValue.add(new CreditCardView(cc.getIssuanceBank(), cc.getNumber())));
            return ResponseEntity.ok(returnValue);
        }

    }

    /***
     * Given a credit card number, efficiently find whether there is a user
     * associated with the credit card If so, return the user id in a 200 OK
     * response. If no such user exists, return 400 Bad Request
     * 
     * @param creditCardNumber
     * @return
     */
    @GetMapping("/credit-card/user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        CreditCard card = creditCardRepository.findByNumber(creditCardNumber).orElse(null);
        if (card == null) {
            return ResponseEntity.badRequest().build();
        } else {
            User user = card.getOwner();
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(user.getId());
        }

    }

    @PostMapping("/credit-card/update-balance")
    public ResponseEntity<String> updateBalance(@RequestBody UpdateBalancePayload[] payload) {
        for (UpdateBalancePayload update : payload) {

            // Find the credit card
            Optional<CreditCard> creditCardOpt = creditCardRepository.findByNumber(update.getCreditCardNumber());
            if (!creditCardOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body("No credit card found for the provided number: " + update.getCreditCardNumber());
            }
            // Get the existing balance history
            CreditCard creditCard = creditCardOpt.get();
            List<BalanceHistory> existingHistory = creditCard.getBalanceHistory();
            List<BalanceHistory> updatedHistory = fillGaps(existingHistory);

            insertUpdate(updatedHistory,
                    new BalanceHistory(update.getBalanceDate(), update.getBalanceAmount(), creditCard));
            creditCard.setBalanceHistory(updatedHistory);
            creditCardRepository.save(creditCard);
        }

        return ResponseEntity.ok("Balance updated successfully.");
    }

    /// Starting from first day fill in the gaps

    private List<BalanceHistory> fillGaps(List<BalanceHistory> balanceHistories) {
        // If the list is empty just do nothing
        if (balanceHistories.isEmpty()) {
            return balanceHistories;
        }
        List<BalanceHistory> updatedHistory = new ArrayList<>(balanceHistories);

        BalanceHistory prev = balanceHistories.get(0);

        // Iterate over the entire list of balances and find gaps
        for (BalanceHistory current : balanceHistories) {
            // Find the gap in number of days between the current balance and the pervious
            long days = ChronoUnit.DAYS.between(prev.getDate(), current.getDate());
            // If the gap between the current balance and the pervious is more than 1 day
            // copy the prev
            if (days > 1) {
                for (int i = 1; i < days; i++) {
                    updatedHistory.add(
                            new BalanceHistory(prev.getDate().plusDays(i), prev.getBalance(), prev.getCreditCard()));
                }
            }
            prev = current;
        }

        // If the last day is not today, add the last value as value till today
        LocalDate today = LocalDate.now();
        BalanceHistory last = balanceHistories.get(balanceHistories.size() - 1);

        long missingDays = ChronoUnit.DAYS.between(last.getDate(), today);
        for (int i = 1; i < missingDays; i++) {
            updatedHistory.add(
                    new BalanceHistory(last.getDate().plusDays(i), last.getBalance(), last.getCreditCard()));
        }
        updatedHistory.sort(null);
        return updatedHistory;
    }

    // Insert the current update
    private void insertUpdate(List<BalanceHistory> balanceHistories, BalanceHistory current) {

        // Find the index of the element that needs to be update
        int index = IntStream.range(0, balanceHistories.size())
                .filter(i -> balanceHistories.get(i).getDate().equals(current.getDate()))
                .findFirst().orElse(balanceHistories.size() - 1);
        if (index < 0) {
            balanceHistories.add(current);
        } else {
            double difference = current.getBalance() - balanceHistories.get(index).getBalance();
            // Update all balances from that point till the end;
            for (int i = index; i < balanceHistories.size(); i++) {
                balanceHistories.get(index).setBalance(balanceHistories.get(index).getBalance() + difference);
            }
        }

    }

}
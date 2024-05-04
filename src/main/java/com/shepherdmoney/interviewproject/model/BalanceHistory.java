package com.shepherdmoney.interviewproject.model;

import java.io.Serializable;
import java.time.LocalDate;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class BalanceHistory implements Comparable<BalanceHistory>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NonNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NonNull
    private double balance;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    private CreditCard creditCard; // Link back to the owning CreditCard

    @Override
    public int compareTo(BalanceHistory o) {
        return this.getDate().compareTo(o.getDate());
    }

    @Override
    public String toString() {
        return "{date:'" + getDate() + "', " + getBalance() + "}";
    }
}

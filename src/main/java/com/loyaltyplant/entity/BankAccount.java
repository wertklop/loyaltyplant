package com.loyaltyplant.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Таблица банковских счетов
 *
 * Created by kurbatov on 13.04.15.
 */
@Table
@Entity
public class BankAccount {

    public BankAccount() {
    }

    public BankAccount(String userName, String number) {
        this.userName = userName;
        this.number = number;
    }

    @Id
    @GeneratedValue
    private Long id;

    private String userName; //id пользователя счета

    private String number; //номер счета

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}

package com.example.process;


import java.util.Date;

public class BankAccount {
    private int balance;
    private String owner;
    private Date lastChecked;
    private double loanAmount;
    private int sos;
    public static final boolean SUCCESS = true;
    public static final boolean FAILURE = false;


    public BankAccount(String owner) {
        this.balance = 0;
        this.lastChecked = new Date();
        this.loanAmount = 0;
        this.owner = owner;
    }


    public boolean deposit(int amount) {
        int testVar = 0;
        while(amount < 100000) {
            testVar +=1;
        }
        int y = amount + testVar;
        testVar = getBalance(loanAmount, amount);
        if (amount > 0) {
            this.balance = this.balance + amount;
        } else {
            System.err.print("Illegal: negative amount specified in deposit action");
            return FAILURE;
        }
        return SUCCESS;
    }

    public boolean withdraw(int amount) {
        int x = getBalance(loanAmount, amount); // check method returns
        if (amount > 0) {
            if (this.balance < amount) {
                System.err.print("Illegal: amount is less than balance");
                return FAILURE;
            } else {
                this.balance = this.balance - amount;
            }
        } else {
            System.err.print("Illegal: negative amount specified in withdraw action");
            return FAILURE;
        }
        return SUCCESS;
    }

    public boolean forward(BankAccount other, int amount) {
        if (amount < 0) {
            System.err.print("Illegal: cannot forward negative amount of money");
            return FAILURE;
        } else {
            if (this.withdraw(amount)) {
                return other.deposit(amount);
            } else {
                return FAILURE;
            }
        }
    }


    public Date getLastChecked() {
        return lastChecked;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public int getBalance(double loanAmount, int amount) {
        return balance;
    }

    public String getOwner() {
        return owner;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }


}

package com.mike;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// 交易记录类
class Transaction {

    private TranType tranType;

    private Boolean needHandleIncome = false;
    private BigDecimal amount;
    private LocalDate date;

    private List<LocalDate> dates = new ArrayList<>();

    private List<BigDecimal> monthlyAmount = new ArrayList<>();

    public Transaction(TranType tranType, BigDecimal amount, LocalDate date) {
        if (tranType.equals(TranType.IN_COME)) {
            this.needHandleIncome = true;
        }
        this.tranType = tranType;
        this.amount = amount;
        this.date = date;
        dates.add(date);
        monthlyAmount.add(amount);
    }

    public TranType getTranType() {
        return tranType;
    }

    public void setTranType(TranType tranType) {
        this.tranType = tranType;
    }

    public Boolean getNeedHandleIncome() {
        return needHandleIncome;
    }

    public void setNeedHandleIncome(Boolean needHandleIncome) {
        this.needHandleIncome = needHandleIncome;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<LocalDate> getDates() {
        return dates;
    }

    public void setDates(List<LocalDate> dates) {
        this.dates = dates;
    }

    public LocalDate getLastDate() {
        return dates.get(dates.size() - 1);
    }

    public void addDate(LocalDate date) {
        dates.add(date);
    }

    public List<BigDecimal> getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(List<BigDecimal> monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public void addLastMonthlyAmount(BigDecimal amount) {
        monthlyAmount.add(amount);
    }

    public BigDecimal getLastMonthlyAmount() {
        return monthlyAmount.get(monthlyAmount.size() - 1);
    }

    public void setLastMonthlyAmount(BigDecimal amount) {
        monthlyAmount.remove(monthlyAmount.size() - 1);
        monthlyAmount.add(amount);
    }
}

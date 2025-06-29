package com.mike;

import com.sun.deploy.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class InterestCalculator {

    private static final BigDecimal DAILY_INTEREST_RATE = new BigDecimal("0.0005"); // 万分之五的日利率
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Integer COUNT_DATE = 26;

    public static void main(String[] args) {
        // 初始化输入数据
        List<Transaction> transactions = new ArrayList<>();

        transactions.add(new Transaction(TranType.OUT_COME, new BigDecimal("5000"), LocalDate.parse("2019-06-11", DATE_FORMATTER)));
        transactions.add(new Transaction(TranType.OUT_COME, new BigDecimal("5000"), LocalDate.parse("2019-08-18", DATE_FORMATTER)));
        transactions.add(new Transaction(TranType.OUT_COME, new BigDecimal("7600"), LocalDate.parse("2019-08-19", DATE_FORMATTER)));
        transactions.add(new Transaction(TranType.OUT_COME, new BigDecimal("17000"),LocalDate.parse("2019-08-23", DATE_FORMATTER)));
        transactions.add(new Transaction(TranType.OUT_COME, new BigDecimal("7000"), LocalDate.parse("2020-01-11", DATE_FORMATTER)));
        transactions.add(new Transaction(TranType.OUT_COME, new BigDecimal("11500"),LocalDate.parse("2020-01-16", DATE_FORMATTER)));
        transactions.add(new Transaction(TranType.OUT_COME, new BigDecimal("3500"), LocalDate.parse("2020-02-03", DATE_FORMATTER)));

        transactions.add(new Transaction(TranType.IN_COME, new BigDecimal("3000"), LocalDate.parse("2024-04-07", DATE_FORMATTER)));
        transactions.add(new Transaction(TranType.IN_COME, new BigDecimal("5000"), LocalDate.parse("2024-05-25", DATE_FORMATTER)));

        // 计算并输出每月金额和总金额
        calculateAndPrintMonthlyAmounts(transactions);

        calculateAmounts(transactions);
    }

    // 利滚利的情况
    private static void calculateAndPrintMonthlyAmounts(List<Transaction> transactions) {
        LocalDate currentDate = LocalDate.now(); // 获取当前日期

        List<LocalDate> dates = getDates(transactions.get(0).getDate(), currentDate);


        // 循环遍历每个月
        for (int i = 0; i < dates.size(); i++) {
            BigDecimal monthlyAmount = BigDecimal.ZERO;

            LocalDate date = dates.get(i);

            // 遍历交易记录，计算当月利息
            if (date.getDayOfMonth() == COUNT_DATE || i == dates.size() - 1) {
                for (Transaction transaction : transactions) {
                    if (transaction.getTranType().equals(TranType.OUT_COME)) {
                        LocalDate lastDate = transaction.getDates().get(transaction.getDates().size() - 1);
                        if (lastDate.isBefore(date)) {
                            long datesBetween = ChronoUnit.DAYS.between(lastDate, date);

                            BigDecimal lastMonthlyAmount = transaction.getLastMonthlyAmount();
                            // 计算利息
                            BigDecimal interest = lastMonthlyAmount.multiply(DAILY_INTEREST_RATE).multiply(new BigDecimal(datesBetween));

                            // 将利息加入当月金额
                            lastMonthlyAmount = lastMonthlyAmount.add(interest);
                            transaction.addDate(date);
                            transaction.addLastMonthlyAmount(lastMonthlyAmount);

                            monthlyAmount = monthlyAmount.add(lastMonthlyAmount);
                        }
                    }
                }

                // 输出当月金额
                System.out.println(date + "   月金额：" + monthlyAmount.setScale(2, RoundingMode.CEILING));
            }

            transactions.stream().filter(transaction ->
                            transaction.getNeedHandleIncome()
                            && transaction.getTranType().equals(TranType.IN_COME)
                            && transaction.getDate().equals(date))
                    .forEach(incomeTran -> {
                        for (Transaction transaction : transactions) {
                            if (transaction.getTranType().equals(TranType.OUT_COME)) {
                                BigDecimal lastMonthlyAmount = transaction.getLastMonthlyAmount();
                                BigDecimal inComeTranAmount = getRealReturnBetweenTwoDay(incomeTran.getAmount(), transaction.getLastDate(), date);
                                if (lastMonthlyAmount.compareTo(inComeTranAmount) >= 0) {
                                    // 符合提前还款条件
                                    transaction.addDate(date);


                                    transaction.addLastMonthlyAmount(lastMonthlyAmount.subtract(inComeTranAmount));
                                    System.out.println("还款处理：" + date + "->" + transaction.getDate() + "->" + incomeTran.getAmount() + "->" + inComeTranAmount);
                                    System.out.println("还款处理：" + lastMonthlyAmount.setScale(2, RoundingMode.CEILING) + "->" + transaction.getLastMonthlyAmount().setScale(2, RoundingMode.CEILING));
                                    transaction.setNeedHandleIncome(false);
                                    break;
                                }
                            }
                        }
                    });


        }

        transactions.stream().filter(transaction -> transaction.getTranType().equals(TranType.OUT_COME)).forEach(transaction -> {

            List<String> itemList = new ArrayList<>();
            for (int i = 0; i < transaction.getDates().size(); i++) {
                long d = i == 0 ? 0L : ChronoUnit.DAYS.between(transaction.getDates().get(i - 1), transaction.getDates().get(i));
                String item = transaction.getDates().get(i).toString() + "(" + d + "|" + transaction.getMonthlyAmount().get(i).setScale(2, RoundingMode.CEILING).toString() + ")";
                itemList.add(item);
            }
            System.out.println(StringUtils.join(itemList, "->"));
        });
        // 将当月金额加入总金额
        BigDecimal totalAmount = transactions.stream().map(Transaction::getLastMonthlyAmount).reduce(BigDecimal::add).get();

        // 输出总金额
        System.out.println("总金额：" + totalAmount.setScale(2, RoundingMode.CEILING));
    }

    // 非利滚利的情况
    private static void calculateAmounts(List<Transaction> transactions) {
        LocalDate today = LocalDate.now(); // 获取当前日期

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if (transaction.getTranType().equals(TranType.OUT_COME)) {
                LocalDate date = transaction.getDate();

                long datesBetween = ChronoUnit.DAYS.between(date, today);

                BigDecimal lastMonthlyAmount = transaction.getAmount();
                // 计算利息
                BigDecimal interest = lastMonthlyAmount.multiply(DAILY_INTEREST_RATE).multiply(new BigDecimal(datesBetween));

                totalAmount = totalAmount.add(lastMonthlyAmount.add(interest));
            }
        }

        // 输出总金额
        System.out.println("总金额：" + totalAmount.setScale(2, RoundingMode.CEILING));
    }


    // 获取从开始日期到结束日期之间所有 26 号的日期
    private static List<LocalDate> getDates(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();

        // 循环遍历开始日期到结束日期之间的每一天
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dates.add(date);
        }

        return dates;
    }

    // 获取还款额实际还款额
    // n + n * 0.0005 * day = inComeTranAmount;
    // n = inComeTranAmount / ( 1 + 0.0005 * day)
    private static BigDecimal getRealReturnBetweenTwoDay(BigDecimal amount, LocalDate startDate, LocalDate endDate) {
        long datesBetween = ChronoUnit.DAYS.between(startDate, endDate);

        BigDecimal divisor = new BigDecimal(1).add(new BigDecimal(datesBetween).multiply(DAILY_INTEREST_RATE));

        BigDecimal divide = amount.divide(divisor, 2, RoundingMode.HALF_UP);
        System.out.println("还款金额:" + amount + " 天数:" + datesBetween + " 利息率:" + divisor + " 实际还款金额:" + divide);
        return divide;
    }
}



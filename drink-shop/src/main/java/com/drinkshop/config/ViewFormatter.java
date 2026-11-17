package com.drinkshop.config;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Hàm định dạng dùng trong Thymeleaf: ${@fmt.money(...)}, ${@fmt.dt(...)}, ${@fmt.date(...)}.
 */
@Component("fmt")
public class ViewFormatter {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HTML_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /** 35000 -> "35.000". */
    public String money(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);
        return format.format(amount);
    }

    /** 35000 -> "35.000đ". */
    public String vnd(BigDecimal amount) {
        return money(amount) + "đ";
    }

    public String dt(LocalDateTime value) {
        return value == null ? "" : DATE_TIME.format(value);
    }

    public String date(LocalDate value) {
        return value == null ? "" : DATE.format(value);
    }

    public String time(LocalDateTime value) {
        return value == null ? "" : TIME.format(value);
    }

    /** Định dạng cho <input type="datetime-local">: 2026-11-16T08:00. */
    public String htmlDate(LocalDateTime value) {
        return value == null ? "" : HTML_DATE.format(value);
    }
}

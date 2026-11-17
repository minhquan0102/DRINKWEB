package com.drinkshop.service;

import com.drinkshop.model.Voucher;
import com.drinkshop.model.dto.VoucherResult;

import java.math.BigDecimal;
import java.util.List;

/** Nghiệp vụ mã giảm giá. */
public interface VoucherService {

    /** Kiểm tra mã và tính số tiền được giảm cho đơn có trị giá subtotal. */
    VoucherResult validate(String code, BigDecimal subtotal);

    List<Voucher> all();

    Voucher getById(Integer id);

    Voucher save(Voucher voucher);

    void delete(Integer id);
}

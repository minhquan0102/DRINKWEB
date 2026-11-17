package com.drinkshop.service.impl;

import com.drinkshop.model.DiscountType;
import com.drinkshop.model.Voucher;
import com.drinkshop.model.dto.VoucherResult;
import com.drinkshop.repository.VoucherRepository;
import com.drinkshop.service.VoucherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class VoucherServiceImpl implements VoucherService {

    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private final VoucherRepository voucherRepository;

    public VoucherServiceImpl(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResult validate(String code, BigDecimal subtotal) {
        if (code == null || code.isBlank()) {
            return VoucherResult.error("Vui lòng nhập mã giảm giá.");
        }
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(code.trim()).orElse(null);
        if (voucher == null) {
            return VoucherResult.error("Mã giảm giá không tồn tại.");
        }
        if (!voucher.isActive()) {
            return VoucherResult.error("Mã giảm giá đã ngừng áp dụng.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            return VoucherResult.error("Mã giảm giá chưa tới thời gian sử dụng.");
        }
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            return VoucherResult.error("Mã giảm giá đã hết hạn.");
        }
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            return VoucherResult.error("Mã giảm giá đã hết lượt sử dụng.");
        }
        if (subtotal == null || subtotal.compareTo(voucher.getMinOrderAmount()) < 0) {
            return VoucherResult.error("Đơn hàng tối thiểu "
                    + MONEY.format(voucher.getMinOrderAmount()) + "đ để dùng mã này.");
        }

        BigDecimal discount;
        if (voucher.getDiscountType() == DiscountType.PERCENT) {
            discount = subtotal.multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                discount = voucher.getMaxDiscount();
            }
        } else {
            discount = voucher.getDiscountValue();
        }
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        return VoucherResult.ok("Đã áp dụng mã " + voucher.getCode() + " (-" + MONEY.format(discount) + "đ)",
                voucher.getId(), discount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Voucher> all() {
        return voucherRepository.findAllByOrderByCodeAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Voucher getById(Integer id) {
        return voucherRepository.findById(id).orElse(null);
    }

    @Override
    public Voucher save(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    @Override
    public void delete(Integer id) {
        voucherRepository.deleteById(id);
    }
}

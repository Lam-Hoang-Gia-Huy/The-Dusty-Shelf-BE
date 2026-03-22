package com.example.JWTImplemenation.Service;

import com.example.JWTImplemenation.Entities.Voucher;
import com.example.JWTImplemenation.Repository.VoucherRepository;
import com.example.JWTImplemenation.Service.IService.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherService implements IVoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    public Optional<Voucher> findByCode(String code) {
        return voucherRepository.findByCodeAndStatusTrue(code); // Modified to check status
    }

    public List<Voucher> findAll() {
        return voucherRepository.findAll();
    }

    public Voucher save(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    public void incrementUsage(Voucher voucher) {
        if (voucher.getCurrentUsage() < voucher.getMaxUsage()) {
            voucher.setCurrentUsage(voucher.getCurrentUsage() + 1);
            voucherRepository.save(voucher);
        } else {
            throw new IllegalArgumentException("Voucher usage limit exceeded");
        }
    }

    public void deactivateVoucher(Integer id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid voucher ID"));
        voucher.setStatus(false);
        voucherRepository.save(voucher);
    }

    public void approveVoucher(Integer id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid voucher ID"));
        voucher.setStatus(true);
        voucherRepository.save(voucher);
    }

    public List<Voucher> findAllAvailableVouchers() {
        Timestamp currentDate = new Timestamp(System.currentTimeMillis());
        return voucherRepository.findAllAvailableVouchers(currentDate);
    }
}

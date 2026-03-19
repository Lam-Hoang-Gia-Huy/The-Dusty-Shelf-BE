package com.example.JWTImplemenation.Controller;

import com.example.JWTImplemenation.Entities.Voucher;
import com.example.JWTImplemenation.Service.IService.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/voucher")
public class VoucherController {

    @Autowired
    private IVoucherService voucherService;

    @GetMapping
    public List<Voucher> getAllVouchers() {
        return voucherService.findAll();
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<String> deactivateVoucher(@PathVariable("id") Integer id) {
        voucherService.deactivateVoucher(id);
        return ResponseEntity.ok("Voucher deactivated");
    }

    @PostMapping
    public Voucher createVoucher(@RequestBody Voucher voucher) {
        voucher.setStatus(false); // Default to false when created
        return voucherService.save(voucher);
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<String> approveVoucher(@PathVariable("id") Integer id) {
        voucherService.approveVoucher(id);
        return ResponseEntity.ok("Voucher approved");
    }

    @GetMapping("/validate")
    public ResponseEntity<Voucher> validateVoucher(@RequestParam("code") String code) {
        Optional<Voucher> voucher = voucherService.findByCode(code);
        return voucher.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(404).build());
    }

    @GetMapping("/available")
    public List<Voucher> getAllAvailableVouchers() {
        return voucherService.findAllAvailableVouchers();
    }
}

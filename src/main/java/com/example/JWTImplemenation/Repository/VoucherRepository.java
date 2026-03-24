package com.example.JWTImplemenation.Repository;

import com.example.JWTImplemenation.Entities.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Optional<Voucher> findByCodeAndStatusTrue(String code);

    Optional<Voucher> findByCode(String voucherCode);
    @Query("SELECT v FROM Voucher v WHERE v.status = true AND v.currentUsage < v.maxUsage AND v.startDate <= :currentDate AND v.endDate >= :currentDate")
    List<Voucher> findAllAvailableVouchers(@Param("currentDate") Timestamp currentDate);
}

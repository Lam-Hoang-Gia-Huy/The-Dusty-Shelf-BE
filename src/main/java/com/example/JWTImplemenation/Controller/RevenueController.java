package com.example.JWTImplemenation.Controller;

import com.example.JWTImplemenation.Service.IService.IRevenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/revenue")
public class RevenueController {

    @Autowired
    private IRevenueService revenueService;

    @GetMapping("/monthly/{year}")
    public ResponseEntity<Map<String, Object>> getMonthlyRevenue(@PathVariable("year") int year) {
        return ResponseEntity.ok(revenueService.calculateRevenueByMonth(year));
    }

    @GetMapping("/yearly")
    public ResponseEntity<Map<String, Object>> getYearlyRevenue() {
        return ResponseEntity.ok(revenueService.calculateRevenueByYear());
    }

    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> getRevenueByDateRange(
            @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        return ResponseEntity.ok(revenueService.calculateRevenueByDateRange(startDate, endDate));
    }
}

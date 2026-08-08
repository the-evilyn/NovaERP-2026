package com.novaerp.dashboard.controller;

import com.novaerp.dashboard.dto.DashboardStatsDTO;
import com.novaerp.dashboard.dto.RecentActivityDTO;
import com.novaerp.dashboard.dto.SalesTrendDTO;
import com.novaerp.dashboard.dto.TopProductDTO;
import com.novaerp.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard & Analytics", description = "Endpoints for executive KPIs, sales charts, inventory health, and recent operations")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Get executive dashboard statistics", description = "Aggregates revenue, purchases, stock valuation, and count indicators")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/sales-trends")
    @Operation(summary = "Get monthly sales and purchases trends", description = "Returns historical monthly comparisons for graphical representation")
    public ResponseEntity<List<SalesTrendDTO>> getSalesTrends() {
        return ResponseEntity.ok(dashboardService.getSalesTrends());
    }

    @GetMapping("/top-products")
    @Operation(summary = "Get top selling products", description = "Retrieves best-performing products by volume and revenue")
    public ResponseEntity<List<TopProductDTO>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(dashboardService.getTopProducts(limit));
    }

    @GetMapping("/recent-activities")
    @Operation(summary = "Get recent enterprise activities", description = "Returns unified timeline of sales, purchases, and settlements")
    public ResponseEntity<List<RecentActivityDTO>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(dashboardService.getRecentActivities(limit));
    }
}

package com.novaerp.dashboard.service;

import com.novaerp.dashboard.dto.DashboardStatsDTO;
import com.novaerp.dashboard.dto.RecentActivityDTO;
import com.novaerp.dashboard.dto.SalesTrendDTO;
import com.novaerp.dashboard.dto.TopProductDTO;

import java.util.List;

public interface DashboardService {
    DashboardStatsDTO getDashboardStats();
    List<SalesTrendDTO> getSalesTrends();
    List<TopProductDTO> getTopProducts(int limit);
    List<RecentActivityDTO> getRecentActivities(int limit);
}

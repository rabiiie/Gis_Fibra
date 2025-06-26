package com.appfibra.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import org.springframework.ui.Model;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardData() {
        Map<String, Object> response = new HashMap<>();
        response.put("constructionPerc", 75.5);
        response.put("documentationPerc", 82.3);
        response.put("complaints", 4);
        return response;
    }
    @GetMapping("/dashboard/stats")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalWorks", 1750);
        stats.put("totalMeters", 566000);
        stats.put("activeProjects", 66);
        stats.put("completedProjects", 357);
        return stats;
    }

}

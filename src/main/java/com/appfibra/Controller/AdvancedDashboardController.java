package com.appfibra.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdvancedDashboardController {

    @GetMapping("/dashboard-general")
    public String showDashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        return "dashboard"; // Thymeleaf buscará dashboard.html en /templates
    }

}

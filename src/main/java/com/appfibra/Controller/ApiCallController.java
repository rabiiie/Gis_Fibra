package com.appfibra.Controller;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiCallController {

    @GetMapping("/api-calls")
    public String showApiCallsPage(Model model) {
        model.addAttribute("pageTitle", "API Calls");
        return "apis"; // Corresponde al nombre del archivo Thymeleaf sin .html
    }
}

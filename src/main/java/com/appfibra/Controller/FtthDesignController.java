package com.appfibra.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/clients")
public class FtthDesignController {

    @GetMapping
    public String showClientsPage(Model model) {

        model.addAttribute("pageTitle", "Clients");

        return "clients";
    }
}
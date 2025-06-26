package com.appfibra.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

	@GetMapping("/home")
	public String home(Model model, HttpServletRequest request) {
	    model.addAttribute("currentPath", request.getRequestURI());
	    return "home";
	}


    @GetMapping("/contact")
    public String showContactPage(Model model) {
        model.addAttribute("pageTitle", "Contact Form");
        return "contact"; // Renderiza templates/contact.html
    }

    @GetMapping("/help")
    public String showHelpPage(Model model) {
        model.addAttribute("pageTitle", "Help Section");
        return "help"; // Renderiza templates/help.html
    }
}

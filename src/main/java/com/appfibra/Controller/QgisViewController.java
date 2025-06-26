package com.appfibra.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class QgisViewController {

    @GetMapping("/qgis-projects-map")
    public String showMapPage(@RequestParam(required = false) String village, Model model) {
        model.addAttribute("village", village);
        return "qgis_projects"; // 🔹 Thymeleaf buscará qgis_projects.html en /templates/
    }
}

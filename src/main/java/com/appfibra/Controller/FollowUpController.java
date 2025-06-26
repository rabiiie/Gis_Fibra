package com.appfibra.Controller;

import com.appfibra.service.ReportPlanrService;
import com.appfibra.service.ViewComparisonService;
import com.appfibra.service.WochenmeldungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class FollowUpController {

    private final WochenmeldungService wochenmeldungService;
    private final ReportPlanrService reportPlanrService;
    private final ViewComparisonService viewComparisonService;

    @Autowired
    public FollowUpController(WochenmeldungService wochenmeldungService,
                              ReportPlanrService reportPlanrService,
                              ViewComparisonService viewComparisonService) {
        this.wochenmeldungService = wochenmeldungService;
        this.reportPlanrService = reportPlanrService;
        this.viewComparisonService = viewComparisonService;
    }

    // === Página principal Follow-up ===
    @GetMapping("/follow-up")
    public String followUpPage(Model model) {
        model.addAttribute("title", "Follow Up");

        List<String> operadores = List.of("Glasfaser Plus", "Deutsche Glasfaser", "UGG");
        model.addAttribute("operadores", operadores);
        model.addAttribute("pageTitle", "Follow Up");

        return "followUp";
    }


    // === Reporte Wochenmeldung ===
    @GetMapping("/reports")
    public String showReports(Model model) {
        model.addAttribute("pageTitle", "Reports Hub");
        return "reports";
    }

    @GetMapping("/reports/wm")
    public String showWochenmeldung(
            @RequestParam(name = "week1", required = false) Integer week1,
            @RequestParam(name = "week2", required = false) Integer week2,
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "tipo", required = false, defaultValue = "POP") String tipo,
            Model model
    ) {
        model.addAttribute("week1", week1);
        model.addAttribute("week2", week2);
        model.addAttribute("year", year);
        model.addAttribute("tipo", tipo);

        if (week1 != null && week2 != null && year != null) {
            List<Object[]> resultados;
            int totalKw1 = 0;
            int totalKw2 = 0;

            if ("DP".equalsIgnoreCase(tipo)) {
                resultados = wochenmeldungService.getProduccionAgrupadaPorDP(week1, week2, year);
                for (Object[] row : resultados) {
                    totalKw1 += ((Number) row[4]).intValue();
                    totalKw2 += ((Number) row[5]).intValue();
                }
            } else {
                resultados = wochenmeldungService.getProduccionAgrupadaPorPOP(week1, week2, year);
                for (Object[] row : resultados) {
                    totalKw1 += ((Number) row[3]).intValue();
                    totalKw2 += ((Number) row[4]).intValue();
                }
            }

            model.addAttribute("resultados", resultados);
            model.addAttribute("totalKw1", totalKw1);
            model.addAttribute("totalKw2", totalKw2);
        }
        model.addAttribute("pageTitle", "Wochenmeldung");

        return "wochenmeldung";
    }

    // === Reporte PLANR ===
    @GetMapping("/reports/planr")
    public String showReportPlanrForm(@RequestParam(name = "pop", required = false) String pop, Model model) {
        model.addAttribute("pop", pop);

        List<String> listaPops = reportPlanrService.getDistinctPops();
        model.addAttribute("listaPops", listaPops);

        if (pop != null && !pop.isEmpty()) {
            List<Map<String, Object>> statusCounts = reportPlanrService.getStatusCountByPop(pop);
            Map<String, Integer> deliveredResumen = new LinkedHashMap<>();
            Map<String, Integer> notDeliveredResumen = new LinkedHashMap<>();
            for (Map<String, Object> row : statusCounts) {
                String deliveryStatus = String.valueOf(row.get("delivery_status"));
                String anschlussstatus = String.valueOf(row.get("anschlussstatus"));
                int count = ((Number) row.get("count")).intValue();
                if ("Delivered".equalsIgnoreCase(deliveryStatus)) {
                    deliveredResumen.put(anschlussstatus, count);
                } else {
                    notDeliveredResumen.put(anschlussstatus, count);
                }
            }

            model.addAttribute("deliveredResumen", deliveredResumen);
            model.addAttribute("notDeliveredResumen", notDeliveredResumen);

            int realUnits = deliveredResumen.values().stream().mapToInt(Integer::intValue).sum();
            int planUnits = notDeliveredResumen.values().stream().mapToInt(Integer::intValue).sum();
            model.addAttribute("realUnits", realUnits);
            model.addAttribute("planUnits", planUnits);

            double pctUnits = planUnits > 0 ? (100.0 * realUnits / planUnits) : 0.0;
            model.addAttribute("unitsPct", String.format("%.1f%%", pctUnits));

            List<Map<String, Object>> grundnaRaw = reportPlanrService.getGrundnaCountByPop(pop);
            int totalGrundna = grundnaRaw.stream().mapToInt(r -> ((Number) r.get("count")).intValue()).sum();
            List<Map<String, Object>> grundnaWithPct = new ArrayList<>();
            for (Map<String, Object> row : grundnaRaw) {
                String grundna = String.valueOf(row.get("grundna"));
                int count = ((Number) row.get("count")).intValue();
                double pct = totalGrundna > 0 ? (100.0 * count / totalGrundna) : 0.0;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("grundna", grundna);
                m.put("count", count);
                m.put("pct", String.format("%.1f%%", pct));
                grundnaWithPct.add(m);
            }

            model.addAttribute("grundnaRows", grundnaWithPct);
            model.addAttribute("totalGrundna", totalGrundna);
        }
        model.addAttribute("pageTitle", "Planrrr Summary");

        return "reportplanr";
    }

    @GetMapping(path = "/reports/planr/details/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> detailsByStatus(
            @RequestParam String pop,
            @RequestParam String deliveryStatus,
            @RequestParam String anschlussstatus
    ) {
        return reportPlanrService.getDpCountByStatus(pop, deliveryStatus, anschlussstatus);
    }

    @GetMapping(path = "/reports/planr/details/grundna", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> detailsByGrundna(
            @RequestParam String pop,
            @RequestParam String grundna
    ) {
        return reportPlanrService.getDpCountByGrundna(pop, grundna);
    }

    // === Comparación Arweb - Planr ===
    @GetMapping("/comparison")
    public String showComparison(@RequestParam(required = false) String pop, Model model) {
        LocalDateTime utcNow = LocalDateTime.now(ZoneOffset.UTC);
        String headerInfo = String.format(
                "Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): %s\nCurrent User's Login: aa000031",
                utcNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        model.addAttribute("headerInfo", headerInfo);
        model.addAttribute("selectedPop", pop);

        List<Map<String, Object>> popStats = viewComparisonService.getPopStatistics();
        model.addAttribute("popStats", popStats);

        List<Map<String, Object>> comparisons = new ArrayList<>();
        Map<String, Object> popDetails = null;
        List<String> dpList = new ArrayList<>();
        Map<String, Integer> dpUnitMap = new LinkedHashMap<>();

        if (pop != null && !pop.isBlank()) {
            List<Map<String, Object>> rawComparisons = viewComparisonService.getComparison(pop);

            comparisons = rawComparisons.stream()
                    .filter(row -> {
                        if (row == null || row.get("home_id") == null) return false;
                        return !Objects.equals(row.get("dp_ar"), row.get("dp_planr")) ||
                                !Objects.equals(row.get("reason_nc"), row.get("grundna")) ||
                                !Objects.equals(row.get("delivery_status"), row.get("anschlussstatus")) ||
                                !Objects.equals(row.get("cable_id"), row.get("kabel_id_from_engineering")) ||
                                !Objects.equals(row.get("odfcatv"), row.get("odf_catv_from_engineering")) ||
                                !Objects.equals(row.get("odfcatv_position"), row.get("odf_catv_position_from_engineering")) ||
                                !Objects.equals(row.get("odf"), row.get("odf_ip_from_engineering")) ||
                                !Objects.equals(row.get("odf_position"), row.get("odf_ip_position_from_engineering"));
                    })
                    .collect(Collectors.toList());

            popDetails = popStats.stream()
                    .filter(stat -> pop.equals(stat.get("pop")))
                    .findFirst()
                    .orElse(null);

            model.addAttribute("selectedPopStats", popDetails);

            dpList = comparisons.stream()
                    .map(row -> Objects.toString(row.get("dp_ar"), ""))
                    .filter(dp -> !dp.isBlank())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            Map<String, Long> dpCounts = comparisons.stream()
                    .map(row -> Objects.toString(row.get("dp_ar"), ""))
                    .filter(dp -> !dp.isBlank())
                    .collect(Collectors.groupingBy(dp -> dp, LinkedHashMap::new, Collectors.counting()));

            dpUnitMap = dpCounts.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().intValue(),
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
        }

        model.addAttribute("dpList", dpList);
        model.addAttribute("dpUnitMap", dpUnitMap);
        model.addAttribute("comparisons", comparisons);
        model.addAttribute("pageTitle", "Arweb-Planrrr Check");

        return "comparison";
    }
}

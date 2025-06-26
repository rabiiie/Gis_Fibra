package com.appfibra.service;

import com.appfibra.DAO.ReportPlanrDAO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportPlanrService {

    private final ReportPlanrDAO reportPlanrDAO;

    public ReportPlanrService(ReportPlanrDAO reportPlanrDAO) {
        this.reportPlanrDAO = reportPlanrDAO;
    }

    public List<String> getDistinctPops() {
        return reportPlanrDAO.findDistinctPops();
    }

    public List<Map<String, Object>> getStatusCountByPop(String pop) {
        return reportPlanrDAO.countByAnschlussstatus(pop);
    }

    public List<Map<String, Object>> getGrundnaCountByPop(String pop) {
        return reportPlanrDAO.countByGrundna(pop);
    }

    /** Nuevo: conteo por DP para status */
    public List<Map<String, Object>> getDpCountByStatus(String pop, String deliveryStatus, String anschlussstatus) {
        return reportPlanrDAO.countByDpAndStatus(pop, deliveryStatus, anschlussstatus);
    }

    /** Nuevo: conteo por DP para grundna */
    public List<Map<String, Object>> getDpCountByGrundna(String pop, String grundna) {
        return reportPlanrDAO.countByDpAndGrundna(pop, grundna);
    }

}

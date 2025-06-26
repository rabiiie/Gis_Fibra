package com.appfibra.service;

import com.appfibra.DAO.WochenmeldungDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WochenmeldungService {

    private final WochenmeldungDAO vistaDAO;

    public WochenmeldungService(WochenmeldungDAO vistaDAO) {
        this.vistaDAO = vistaDAO;
    }

    public List<Object[]> getProduccionAgrupadaPorPOP(Integer w1, Integer w2, Integer year) {
        return vistaDAO.findProduccionAgrupadaPorPOP(w1, w2, year);
    }

    public List<Object[]> getProduccionAgrupadaPorDP(Integer w1, Integer w2, Integer year) {
        return vistaDAO.findProduccionAgrupadaPorDP(w1, w2, year);
    }
}

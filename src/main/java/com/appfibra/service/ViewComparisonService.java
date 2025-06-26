package com.appfibra.service;

import com.appfibra.DAO.ViewComparisonDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class ViewComparisonService {

    @Autowired
    private ViewComparisonDao viewComparisonDao;

    public List<Map<String, Object>> getComparison(String pop) {
        return viewComparisonDao.compareViews(pop);
    }

    public List<Map<String, Object>> getPopStatistics() {
        return viewComparisonDao.getPopStatistics();
    }
}
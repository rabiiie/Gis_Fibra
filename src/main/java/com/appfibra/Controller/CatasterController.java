package com.appfibra.Controller;

import com.appfibra.DAO.QgisProjectDAO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cataster")
public class CatasterController {

    private final QgisProjectDAO dao;

    public CatasterController(QgisProjectDAO dao) {
        this.dao = dao;
    }

    @GetMapping("/geojson")
    public Map<String, Object> getCatasterByVillage(@RequestParam String village) {
        String tableName = village + "_cataster";
        return dao.getCatasterGeoJSON(tableName);
    }
    
    @GetMapping("/buildings")
    public ResponseEntity<Map<String, Object>> getBuildingsGeoJSON(@RequestParam String village) {
        return ResponseEntity.ok(dao.getBuildingsGeoJSON(village));
    }

}

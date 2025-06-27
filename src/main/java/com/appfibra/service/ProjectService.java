package com.appfibra.service;

import com.appfibra.DAO.ProjectDAO;
import com.appfibra.DAO.CivilWorksDAO;
import com.appfibra.dto.VillageDTO;

import jakarta.annotation.PostConstruct;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.net.URI;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Service
public class ProjectService {
	

	
    @Autowired
    private ProjectDAO projectDAO;

    @Autowired
    private CivilWorksDAO civilWorksDAO;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getAllProjects() {
        return projectDAO.getAllProjects();
    }

    public Map<String, Object> getProjectById(int projectId) {
        return projectDAO.getProjectById(projectId);
    }

    public List<Map<String, Object>> getCivilWorksByProject(int projectId) {
        return civilWorksDAO.getCivilWorksByProject(projectId);
    }
    
    public List<VillageDTO> getVillagesByProject(Long projectId) {
        String sql = """
            SELECT DISTINCT cw.village
				FROM civil_works cw
				JOIN pops p ON cw.pop_id = p.pop_id
				WHERE p.project_id = ?;

        """;

        return jdbcTemplate.query(sql, new Object[]{projectId}, (rs, rowNum) -> 
            new VillageDTO(rs.getString("village"))
        );
    }
    
    public List<Map<String, Object>> getPopsByProject(Long projectId) {
        String sql = """
            SELECT pop_id AS id, name 
            FROM pops 
            WHERE project_id = ?
            ORDER BY name
        """;
        return jdbcTemplate.queryForList(sql, projectId);
    }

    public List<Map<String, Object>> getProjectsByClientId(int clientId) {
        return projectDAO.getProjectsByClientId(clientId);
    }
    
    public void geocodeMissingProjects() {
        List<Map<String, Object>> projects = projectDAO.getProjectsWithoutCoordinates();
        
        for (Map<String, Object> project : projects) {
            String name = (String) project.get("name");
            Long id = ((Number) project.get("project_id")).longValue();

            Optional<double[]> coords = geocodeProjectName(name);
            coords.ifPresent(latlng -> {
                projectDAO.updateCoordinatesById(id, latlng[0], latlng[1]);
                System.out.println("✔ Coordenadas asignadas a " + name);
            });

            try {
                Thread.sleep(1000); // evita sobrecarga del servidor Nominatim
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public Optional<double[]> geocodeProjectName(String name) {
        try {
            String query = URLEncoder.encode(name + ", ", StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?q=" + query + "&format=json&limit=1";
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "APP_FIBRA")
                .GET()
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray results = new JSONArray(response.body());
            
            if (!results.isEmpty()) {
                JSONObject result = results.getJSONObject(0);
                double lat = result.getDouble("lat");
                double lon = result.getDouble("lon");
                return Optional.of(new double[]{lat, lon});
            }
        } catch (Exception e) {
            System.err.println("❌ Error geocodificando " + name + ": " + e.getMessage());
        }
        return Optional.empty();
    }
    
    @PostConstruct
    public void initGeocoding() {
        List<Map<String, Object>> missing = projectDAO.getProjectsWithoutCoordinates();
        if (!missing.isEmpty()) {
            System.out.println("🛰 Geocodificando " + missing.size() + " proyectos sin coordenadas...");
            geocodeMissingProjects();
        } else {
            System.out.println("✅ Todos los proyectos ya tienen coordenadas.");
        }
    }


}

package com.appfibra.service;

import com.appfibra.DAO.ProjectDAO;
import com.appfibra.DAO.CivilWorksDAO;
import com.appfibra.dto.VillageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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



}

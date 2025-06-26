package com.appfibra.service;

import com.appfibra.DAO.QgisProjectDAO;
import com.appfibra.model.QgisProject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QgisProjectService {

    private final QgisProjectDAO qgisProjectDAO;

    public QgisProjectService(QgisProjectDAO qgisProjectDAO) {
        this.qgisProjectDAO = qgisProjectDAO;
    }

    public List<QgisProject> getAllProjects() {
        return qgisProjectDAO.findAll();
    }

    public QgisProject getProjectById(Long id) {
        return qgisProjectDAO.findById(id);
    }

    public void addProject(QgisProject project) {
        qgisProjectDAO.insert(project);
    }

    public void updateProject(QgisProject project) {
        qgisProjectDAO.update(project);
    }

    public void deleteProject(Long id) {
        qgisProjectDAO.delete(id);
    }
}

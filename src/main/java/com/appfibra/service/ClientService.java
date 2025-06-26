package com.appfibra.service;

import com.appfibra.DAO.ClientDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class ClientService {

    @Autowired
    private ClientDAO clientDAO;

    public List<Map<String, Object>> getAllClients() {
        return clientDAO.getAllClients();
    }

    public List<Map<String, Object>> getProjectsByClient(Long clientId) {
        return clientDAO.getProjectsByClient(clientId);
    }
    
    public Map<String, Object> getClientById(Long clientId) {
        Map<String, Object> client = clientDAO.getClientById(clientId);
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }
        return client;
    }


}

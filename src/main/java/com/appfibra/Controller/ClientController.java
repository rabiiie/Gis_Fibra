package com.appfibra.Controller;

import com.appfibra.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    /**
     * Devuelve todos los clientes. 
     * GET /api/clients
     */
    @GetMapping
    public List<Map<String, Object>> getAllClients() {
        return clientService.getAllClients();
    }

    /**
     * Devuelve los proyectos de un cliente específico.
     * GET /api/clients/{clientId}/projects
     */
    @GetMapping("/{clientId}/projects")
    public List<Map<String, Object>> getProjectsByClient(@PathVariable Long clientId) {
        return clientService.getProjectsByClient(clientId);
    }
    
    @GetMapping("/{clientId}")
    public Map<String, Object> getClientById(@PathVariable Long clientId) {
        return clientService.getClientById(clientId);
    }
}

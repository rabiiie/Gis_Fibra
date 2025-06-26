package com.appfibra.service;

import com.appfibra.dto.ClientHierarchy;
import com.appfibra.dto.Project;
import com.appfibra.dto.VillageLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GeoserverService {

    private static final Logger log = LoggerFactory.getLogger(GeoserverService.class);

    /**
     * Devuelve la jerarquía Cliente → Proyecto → Village desde el GetCapabilities de GeoServer
     */
    public List<ClientHierarchy> getLayerHierarchy() {
        List<ClientHierarchy> hierarchy = new ArrayList<>();

        try {
            String capabilitiesUrl = "http://localhost:8080/geoserver/wms?service=WMS&version=1.3.0&request=GetCapabilities";
            HttpURLConnection conn = (HttpURLConnection) new URL(capabilitiesUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");

            String xmlResponse = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            conn.disconnect();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));

            NodeList layerNodes = doc.getElementsByTagName("Layer");
            for (int i = 0; i < layerNodes.getLength(); i++) {
                Element layerEl = (Element) layerNodes.item(i);

                NodeList nameNodes = layerEl.getElementsByTagName("Name");
                if (nameNodes.getLength() == 0) continue;

                String layerName = nameNodes.item(0).getTextContent(); // Ejemplo: DGF:Meschede_Brenkhausen_buildings
                String[] parts = layerName.split(":");
                if (parts.length != 2) continue;

                String workspace = parts[0]; // DGF
                String layerPart = parts[1]; // Meschede_Brenkhausen_buildings
                String[] nameParts = layerPart.split("_");
                if (nameParts.length < 3) continue; // Nos aseguramos que tenga las 3 partes

                String project = capitalizeFirstLetter(nameParts[0]); // Meschede
                String village = nameParts[1]; // Brenkhausen

                // Buscar o crear ClientHierarchy
                ClientHierarchy ch = hierarchy.stream()
                        .filter(c -> c.getClient().equals(workspace))
                        .findFirst()
                        .orElseGet(() -> {
                            ClientHierarchy c = new ClientHierarchy();
                            c.setClient(workspace);
                            c.setProjects(new ArrayList<>());
                            hierarchy.add(c);
                            return c;
                        });

                // Buscar o crear Project
                Project proj = ch.getProjects().stream()
                        .filter(p -> p.getProject().equals(project))
                        .findFirst()
                        .orElseGet(() -> {
                            Project p = new Project();
                            p.setProject(project);
                            p.setVillages(new ArrayList<>());
                            ch.getProjects().add(p);
                            return p;
                        });

                // Buscar o crear VillageLayer
                VillageLayer villageLayer = proj.getVillages().stream()
                        .filter(v -> v.getVillage().equals(village))
                        .findFirst()
                        .orElseGet(() -> {
                            VillageLayer v = new VillageLayer();
                            v.setVillage(village);
                            v.setLayerNames(new ArrayList<>());
                            proj.getVillages().add(v);
                            return v;
                        });

                // Añadir el nombre real de la capa (con workspace) a la lista de capas del village
                if (!villageLayer.getLayerNames().contains(layerName)) {
                    villageLayer.getLayerNames().add(layerName);
                }
            }

        } catch (Exception e) {
            log.error("Error obteniendo jerarquía de GeoServer", e);
        }

        return hierarchy;
    }

    private String capitalizeFirstLetter(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }


    // Mantienes getLayerFields sin cambios
    public List<String> getLayerFields(String layerName) {
        try {
            String wfsUrl = "http://localhost:8080/geoserver/wfs?" +
                    "service=WFS&version=1.1.0&request=DescribeFeatureType&typeName=" + layerName;
            HttpURLConnection conn = (HttpURLConnection) new URL(wfsUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");

            String xmlResponse = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            conn.disconnect();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));

            NodeList elements = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "element");
            List<String> fields = new ArrayList<>();
            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                String fieldName = element.getAttribute("name");
                if (!fieldName.toLowerCase().matches(".*(geom|bound|the_geom|geometry|shape).*")) {
                    fields.add(fieldName);
                }
            }
            String layerShortName = layerName.split(":")[1];
            fields.removeIf(f -> f.equalsIgnoreCase(layerShortName));
            return fields;

        } catch (Exception e) {
            log.error("Error obteniendo campos para capa: {}", layerName, e);
            return Collections.emptyList();
        }
    }
}

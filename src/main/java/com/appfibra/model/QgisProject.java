package com.appfibra.model;

public class QgisProject {
    private Long id;
    private String name;
    private String description;
    private Double lat;
    private Double lon;

    // Constructor vacío
    public QgisProject() {}

    // Constructor con todos los atributos
    public QgisProject(Long id, String name, String description, Double lat, Double lon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lon = lon;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }
}

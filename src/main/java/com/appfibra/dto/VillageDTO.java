package com.appfibra.dto;

public class VillageDTO {
    private String villageName;

    // Constructor vacío (necesario para Spring)
    public VillageDTO() {}

    // Constructor con parámetros
    public VillageDTO(String villageName) {
        this.villageName = villageName;
    }

    // Getter y Setter
    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }
}

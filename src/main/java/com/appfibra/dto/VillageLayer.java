package com.appfibra.dto;

import java.util.List;

public class VillageLayer {
    private String village;
    private List<String> layerNames;

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public List<String> getLayerNames() {
        return layerNames;
    }

    public void setLayerNames(List<String> layerNames) {
        this.layerNames = layerNames;
    }
}

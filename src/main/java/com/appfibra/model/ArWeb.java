package com.appfibra.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ar_web")
public class ArWeb {
    @Id
    @Column(name = "home_id")
    private Long homeId;

    @Column(name = "dp")
    private String dp;

    @Column(name = "reason_nc")
    private String reasonNc;

    @Column(name = "delivery_status")
    private String deliveryStatus;

    @Column(name = "cable_id")
    private String cableId;

    @Column(name = "odfcatv")
    private String odfcatv;

    @Column(name = "odfcatv_position")
    private String odfcatvPosition;

    @Column(name = "odf")
    private String odf;

    @Column(name = "odf_position")
    private String odfPosition;

    // Constructors
    public ArWeb() {
    }

    // Getters and Setters
    public Long getHomeId() {
        return homeId;
    }

    public void setHomeId(Long homeId) {
        this.homeId = homeId;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getReasonNc() {
        return reasonNc;
    }

    public void setReasonNc(String reasonNc) {
        this.reasonNc = reasonNc;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getCableId() {
        return cableId;
    }

    public void setCableId(String cableId) {
        this.cableId = cableId;
    }

    public String getOdfcatv() {
        return odfcatv;
    }

    public void setOdfcatv(String odfcatv) {
        this.odfcatv = odfcatv;
    }

    public String getOdfcatvPosition() {
        return odfcatvPosition;
    }

    public void setOdfcatvPosition(String odfcatvPosition) {
        this.odfcatvPosition = odfcatvPosition;
    }

    public String getOdf() {
        return odf;
    }

    public void setOdf(String odf) {
        this.odf = odf;
    }

    public String getOdfPosition() {
        return odfPosition;
    }

    public void setOdfPosition(String odfPosition) {
        this.odfPosition = odfPosition;
    }
}
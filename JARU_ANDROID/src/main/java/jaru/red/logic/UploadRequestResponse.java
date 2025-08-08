package jaru.red.logic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import jaru.ori.web.controlcarrera.RegistroLocalizacion;

public class UploadRequestResponse {
    private String cOrder;
    private List<String> lData;
    private List<UploadPunch> lPunch;
    private List<RegistroLocalizacion> lLoc;

    public UploadRequestResponse() {
        cOrder = "";
        lData = new ArrayList<String>();
        lPunch = new ArrayList<UploadPunch>();
        lLoc = new ArrayList<RegistroLocalizacion>();
    }

    public UploadRequestResponse(String cOrder) {
        this.cOrder = cOrder;
        lData = new ArrayList<String>();
        lPunch = new ArrayList<UploadPunch>();
        lLoc = new ArrayList<RegistroLocalizacion>();
    }

    public UploadRequestResponse(String cOrder, List<String> lData) {
        this.cOrder = cOrder;
        this.lData = lData;
        lPunch = new ArrayList<UploadPunch>();
        lLoc = new ArrayList<RegistroLocalizacion>();
    }

    public UploadRequestResponse(String cOrder, List<String> lData, List<UploadPunch> lPunch) {
        this.cOrder = cOrder;
        this.lData = lData;
        this.lPunch = lPunch;
        lLoc = new ArrayList<RegistroLocalizacion>();
    }

    public UploadRequestResponse(String cOrder, List<String> lData, List<UploadPunch> lPunch, List<RegistroLocalizacion> lLoc) {
        this.cOrder = cOrder;
        this.lData = lData;
        this.lPunch = lPunch;
        this.lLoc = lLoc;
    }

    @JsonProperty("order")
    public String getcOrder() {
        return cOrder;
    }
    public void setcOrder(String cOrder) {
        this.cOrder = cOrder;
    }
    @JsonProperty("data")
    public List<String> getlData() {
        return lData;
    }
    public void setlData(List<String> lData) {
        this.lData = lData;
    }
    @JsonProperty("punches")
    public List<UploadPunch> getlPunch() {
        return lPunch;
    }
    public void setlPunch(List<UploadPunch> lPunch) {
        this.lPunch = lPunch;
    }
    @JsonProperty("localizations")
    public List<RegistroLocalizacion> getlLoc() {
        return lLoc;
    }
    public void setlLoc(List<RegistroLocalizacion> lLoc) {
        this.lLoc = lLoc;
    }
}

package jaru.red.logic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class UploadRequestResponse {
    private String cOrder;
    private List<String> lData;
    private List<UploadPunch> lPunch;

    public UploadRequestResponse() {
        cOrder = "";
        lData = new ArrayList<String>();
        lPunch = new ArrayList<UploadPunch>();
    }

    public UploadRequestResponse(String cOrder) {
        this.cOrder = cOrder;
        lData = new ArrayList<String>();
        lPunch = new ArrayList<UploadPunch>();
    }

    public UploadRequestResponse(String cOrder, List<String> lData) {
        this.cOrder = cOrder;
        this.lData = lData;
        lPunch = new ArrayList<UploadPunch>();
    }

    public UploadRequestResponse(String cOrder, List<String> lData, List<UploadPunch> lPunch) {
        this.cOrder = cOrder;
        this.lData = lData;
        this.lPunch = lPunch;
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
}

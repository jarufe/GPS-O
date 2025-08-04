package jaru.red.logic;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UploadPunch {
    private String cStation;
    private String cSicard;
    private String cDate;
    private String cTime;
    private int nType; //0=SiCard; 1=Bib#; 2=Bib# Teams
    private int nBattery;
    private String cReading;
    private String cRaw;

    public UploadPunch() {
        cStation = "";
        cSicard = "";
        cDate = "1970-01-01";
        cTime = "10:30:00";
        nType = 0;
        nBattery = 100;
        cReading = "1970-01-01 10:30:00";
        cRaw = "";
    }

    public UploadPunch(String cStation, String cSicard, String cDate, String cTime, int nType, int nBattery, String cReading, String cRaw) {
        this.cStation = cStation;
        this.cSicard = cSicard;
        this.cDate = cDate;
        this.cTime = cTime;
        this.nType = nType;
        this.nBattery = nBattery;
        this.cReading = cReading;
        this.cRaw = cRaw;
    }
    @JsonProperty("station")
    public String getcStation() {
        return cStation;
    }
    public void setcStation(String cStation) {
        this.cStation = cStation;
    }
    @JsonProperty("sicard")
    public String getcSicard() {
        return cSicard;
    }
    public void setcSicard(String cSicard) {
        this.cSicard = cSicard;
    }
    @JsonProperty("date")
    public String getcDate() {
        return cDate;
    }
    public void setcDate(String cDate) {
        this.cDate = cDate;
    }
    @JsonProperty("time")
    public String getcTime() { return cTime; }
    public void setcTime(String cTime) {
        this.cTime = cTime;
    }
    @JsonProperty("type")
    public int getnType() {
        return nType;
    }
    public void setnType(int nType) {
        this.nType = nType;
    }
    @JsonProperty("battery")
    public int getnBattery() {
        return nBattery;
    }
    public void setnBattery(int nBattery) {
        this.nBattery = nBattery;
    }
    @JsonProperty("reading")
    public String getcReading() {
        return cReading;
    }
    public void setcReading(String cReading) {
        this.cReading = cReading;
    }
    @JsonProperty("raw")
    public String getcRaw() {
        return cRaw;
    }
    public void setcRaw(String cRaw) {
        this.cRaw = cRaw;
    }
    /**
     * Method that converts the string representation in hexadecimal of a punch
     * to an array of bytes
     * @return byte[]
     */
    public byte[] convertirHexaRaw () {
        byte[] vaResul = null;
        if (cRaw!=null) {
            vaResul = new byte[cRaw.length() / 2];
            for (int i = 0; i < vaResul.length; i++) {
                int index = i * 2;
                int j = Integer.parseInt(cRaw.substring(index, index + 2), 16);
                vaResul[i] = (byte) j;
            }
        }
        return vaResul;
    }
}

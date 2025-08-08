package jaru.red.logic;

public class UploadLoc {
    private int eve2cod;
    private int cat2cod;
    private String loccnom;
    private String loccdor;
    private String loctpas;
    private String locclon;
    private String locclat;
    private String loccele;

    public UploadLoc() {
        this.eve2cod = -1;
        this.cat2cod = -1;
        this.loccnom = "";
        this.loccdor = "";
        this.loctpas = "";
        this.locclon = "";
        this.locclat = "";
        this.loccele = "";
    }

    public UploadLoc(int eve2cod, int cat2cod, String loccnom, String loccdor, String loctpas, String locclon, String locclat, String loccele) {
        this.eve2cod = eve2cod;
        this.cat2cod = cat2cod;
        this.loccnom = loccnom;
        this.loccdor = loccdor;
        this.loctpas = loctpas;
        this.locclon = locclon;
        this.locclat = locclat;
        this.loccele = loccele;
    }


    public int getEve2cod() {
        return eve2cod;
    }

    public void setEve2cod(int eve2cod) {
        this.eve2cod = eve2cod;
    }

    public int getCat2cod() {
        return cat2cod;
    }

    public void setCat2cod(int cat2cod) {
        this.cat2cod = cat2cod;
    }

    public String getLoccnom() {
        return loccnom;
    }

    public void setLoccnom(String loccnom) {
        this.loccnom = loccnom;
    }

    public String getLoccdor() {
        return loccdor;
    }

    public void setLoccdor(String loccdor) {
        this.loccdor = loccdor;
    }

    public String getLoctpas() {
        return loctpas;
    }

    public void setLoctpas(String loctpas) {
        this.loctpas = loctpas;
    }

    public String getLocclon() {
        return locclon;
    }

    public void setLocclon(String locclon) {
        this.locclon = locclon;
    }

    public String getLocclat() {
        return locclat;
    }

    public void setLocclat(String locclat) {
        this.locclat = locclat;
    }

    public String getLoccele() {
        return loccele;
    }

    public void setLoccele(String loccele) {
        this.loccele = loccele;
    }


}

package jaru.ori.web.controlcarrera;

/**
 *
 * @author JAVI
 */
public class UsuarioDatos implements java.io.Serializable {
    private String usu1cod;
    private Integer eve2cod;
    private Integer pci2cod;
    private Boolean usubadm;

    public UsuarioDatos() {
        usu1cod = null;
        eve2cod = null;
        pci2cod = null;
        usubadm = null;
    }

    public UsuarioDatos(String cUsuario, Integer nEvento, Integer nPC, Boolean bAdm) {
        this.usu1cod = cUsuario;
        this.eve2cod = nEvento;
        this.pci2cod = nPC;
        this.usubadm = bAdm;
    }

    public String getUsu1cod() {
        return usu1cod;
    }

    public void setUsu1cod(String Usu1cod) {
        this.usu1cod = Usu1cod;
    }

    public Integer getEve2cod() {
        return eve2cod;
    }

    public void setEve2cod(Integer Eve2cod) {
        this.eve2cod = Eve2cod;
    }

    public Integer getPci2cod() {
        return pci2cod;
    }

    public void setPci2cod(Integer Pci2cod) {
        this.pci2cod = Pci2cod;
    }

    public Boolean getUsubadm() {
        return usubadm;
    }

    public void setUsubadm(Boolean Usubadm) {
        this.usubadm = Usubadm;
    }



}

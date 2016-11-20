package com.enviosya.persistence.shipment;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Gonzalo
 */
@Entity
public class ShipmentEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

//    private String descripcion;
//    private String enviaLatitud;
//    private String enviaLongitud;
//    private String recibeLatitud;
//    private String recibeLongitud;
//    private Long   idCadete;
//    private Long idFormaPago;
//    private String imagenPaquete;

   
   @Column(length = 300)
   private String descripcion;
   
   @NotNull
   @Column(length = 300)
   private String origenLatitud;
   
   @NotNull
   @Column(length = 300)
   private String origenLongitud;
   
   @NotNull
   @Column(length = 300)
   private String destinoLatitud;
   
   @NotNull
   @Column(length = 300)
   private String destinoLongitud;
   
   private Long idCadete;
   
   @ManyToOne(fetch=FetchType.LAZY)
   @JoinColumn(name = "WAYTOPAY_ID")
   private WayToPayEntity idWayToPay;
   
   @NotNull
   @Column(length = 500)
   private String imagenPaquete;
   

   public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getOrigenLatitud() {
        return origenLatitud;
    }

    public void setOrigenLatitud(String origenLatitud) {
        this.origenLatitud = origenLatitud;
    }

    public String getOrigenLongitud() {
        return origenLongitud;
    }

    public void setOrigenLongitud(String origenLongitud) {
        this.origenLongitud = origenLongitud;
    }

    public String getDestinoLatitud() {
        return destinoLatitud;
    }

    public void setDestinoLatitud(String destinoLatitud) {
        this.destinoLatitud = destinoLatitud;
    }

    public String getDestinoLongitud() {
        return destinoLongitud;
    }

    public void setDestinoLongitud(String destinoLongitud) {
        this.destinoLongitud = destinoLongitud;
    }

    public Long getIdCadete() {
        return idCadete;
    }

    public void setIdCadete(Long idCadete) {
        this.idCadete = idCadete;
    }

    public WayToPayEntity getIdWayToPay() {
        return idWayToPay;
    }

    public void setIdWayToPay(WayToPayEntity idWayToPay) {
        this.idWayToPay = idWayToPay;
    }

    public String getImagenPaquete() {
        return imagenPaquete;
    }

    public void setImagenPaquete(String imagenPaquete) {
        this.imagenPaquete = imagenPaquete;
    }

        
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ShipmentEntity)) {
            return false;
        }
        ShipmentEntity other = (ShipmentEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.enviosya.persistence.shipment.ShipmentEntity[ id=" + id + " ]";
    }
    
}

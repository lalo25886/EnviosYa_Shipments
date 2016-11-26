package com.enviosya.shipment.persistence;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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

   //@ManyToOne(fetch=FetchType.LAZY)
  // @JoinColumn(name = "WAYTOPAY_ID")
   //private WayToPayEntity idWayToPay;
   private Long idWayToPay;

   @NotNull
   @Column(length = 500)
   private String imagenPaquete;

   private Long idClienteOrigen;

   private Long idClienteDestino;

   private int confirmado;

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

//    public WayToPayEntity getIdWayToPay() {
//        return idWayToPay;
//    }
//
//    public void setIdWayToPay(WayToPayEntity idWayToPay) {
//        this.idWayToPay = idWayToPay;
//    }

    public Long getIdWayToPay() {
        return idWayToPay;
    }

    public void setIdWayToPay(Long idWayToPay) {
        this.idWayToPay = idWayToPay;
    }
    

    public String getImagenPaquete() {
        return imagenPaquete;
    }

    public void setImagenPaquete(String imagenPaquete) {
        this.imagenPaquete = imagenPaquete;
    }

    public Long getIdClienteOrigen() {
        return idClienteOrigen;
    }

    public void setIdClienteOrigen(Long idClienteOrigen) {
        this.idClienteOrigen = idClienteOrigen;
    }

    public Long getIdClienteDestino() {
        return idClienteDestino;
    }

    public void setIdClienteDestino(Long idClienteDestino) {
        this.idClienteDestino = idClienteDestino;
    }

    public int getConfirmado() {
        return confirmado;
    }

    public void setConfirmado(int confirmado) {
        this.confirmado = confirmado;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ShipmentEntity)) {
            return false;
        }
        ShipmentEntity other = (ShipmentEntity) object;
        if ((this.id == null
                && other.id != null)
                || (this.id != null
                && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.enviosya.persistence.shipment.ShipmentEntity[ id=" + id + " ]";
    }
    
}


package com.enviosya.shipment.domain;

import java.util.Objects;

/**
 *
 * @author Gonzalo
 */
public class Shipment {
    private Long id;
    private String descripcion;
    private String origenLatitud;
    private String origenLongitud;
    private String destinoLatitud;
    private String destinoLongitud;
    private Long   idCadete;
    private Long idFormaPago;
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

    public Long getIdFormaPago() {
        return idFormaPago;
    }

    public void setIdFormaPago(Long idFormaPago) {
        this.idFormaPago = idFormaPago;
    }

    public String getImagenPaquete() {
        return imagenPaquete;
    }

    public void setImagenPaquete(String imagenPaquete) {
        this.imagenPaquete = imagenPaquete;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Shipment other = (Shipment) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ID = " + id ;
    }
    
    
}

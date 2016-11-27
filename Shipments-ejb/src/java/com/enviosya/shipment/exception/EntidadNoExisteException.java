/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.enviosya.shipment.exception;

/**
 *
 * @author Gonzalo
 */
public class EntidadNoExisteException extends Exception {

    public EntidadNoExisteException() {
        super();
    }

    public EntidadNoExisteException(String message) {
        super(message);
    }

    public EntidadNoExisteException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntidadNoExisteException(Throwable cause) {
        super(cause);
    }
}

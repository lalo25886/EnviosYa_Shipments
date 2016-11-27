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
public class DatoErroneoException extends Exception{

    public DatoErroneoException() {
        super();
    }

    public DatoErroneoException(String message) {

        super(message);
    }

    public DatoErroneoException(String message, Throwable cause) {

        super(message, cause);
    }

    public DatoErroneoException(Throwable cause) {

        super(cause);
    }
}


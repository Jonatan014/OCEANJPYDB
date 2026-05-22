/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prueba;

/**
 *
 * @author jonat
 */
public class ErrorSintactico extends Exception {

    public ErrorSintactico(String mensaje) {
        super(
            "java.lang.ErrorSintactico:\n" +
            mensaje
        );
    }
}
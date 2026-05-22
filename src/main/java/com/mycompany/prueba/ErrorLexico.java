/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prueba;

/**
 *
 * @author jonat
 */

public class ErrorLexico extends Exception {

    public ErrorLexico(String mensaje) {
        super(
            "java.lang.ErrorLexico:\n" +
            mensaje
        );
    }
}
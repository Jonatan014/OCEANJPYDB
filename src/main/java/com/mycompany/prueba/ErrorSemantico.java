/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prueba;

/**
 *
 * @author jonat
 */

public class ErrorSemantico extends Exception {

    public ErrorSemantico(String mensaje) {
        super(
            "java.lang.ErrorSemantico:\n" +
            mensaje
        );
    }
}
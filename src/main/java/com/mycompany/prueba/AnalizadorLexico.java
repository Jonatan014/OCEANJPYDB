/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prueba;

/**
 *
 * @author jarqu
 */

import java.util.ArrayList;

public class AnalizadorLexico {

    private ArrayList<Token> tokens = new ArrayList<>();
    private int linea = 1;

    public ArrayList<Token> analizar(String codigo) {

        tokens.clear();
        linea = 1;

        for (int i = 0; i < codigo.length(); i++) {
            char c = codigo.charAt(i);

            if (c == '\n') {
                linea++;
                continue;
            }

            if (Character.isWhitespace(c)) continue;

            if (Character.isLetter(c)) {
                String lexema = "";

                while (i < codigo.length() &&
                       Character.isLetterOrDigit(codigo.charAt(i))) {
                    lexema += codigo.charAt(i);
                    i++;
                }
                i--;

                if (lexema.equals("Trucha") ||
                    lexema.equals("Camaron") ||
                    lexema.equals("Salmon")) {

                    tokens.add(new Token("TIPO_DATO", lexema, linea));

                } else if (lexema.equals("mostrar")) {

                    tokens.add(new Token("PALABRA_RESERVADA", lexema, linea));

                } else {

                    tokens.add(new Token("IDENTIFICADOR", lexema, linea));
                }
            }

            
            else if (Character.isDigit(c)) {
                String numero = "";
                boolean decimal = false;

                while (i < codigo.length()) {
                    char actual = codigo.charAt(i);

                    if (Character.isDigit(actual)) {
                        numero += actual;
                    }
                    else if (actual == '.' && !decimal) {
                        decimal = true;
                        numero += actual;
                    }
                    else break;

                    i++;
                }
                i--;

                tokens.add(new Token("NUMERO", numero, linea));
            }

            
            else if (c == '"') {
                String texto = "";
                i++;

                while (i < codigo.length() && codigo.charAt(i) != '"') {
                    texto += codigo.charAt(i);
                    i++;
                }

                tokens.add(new Token("STRING", texto, linea));
            }

            else if (c == '~') {
                tokens.add(new Token("OPERADOR", "~", linea));
            }
            else if (c == '<') {
                tokens.add(new Token("OPERADOR", "<", linea));
            }
            else if (c == '>') {
                tokens.add(new Token("OPERADOR", ">", linea));
            }
            else if (c == '$') {
                tokens.add(new Token("OPERADOR", "$", linea));
            }
            else if (c == '%') {
                tokens.add(new Token("OPERADOR", "%", linea));
            }

            else if (c == ';') {
                tokens.add(new Token("DELIMITADOR", ";", linea));
            }
            
            else if (c == '(' || c == ')') {
                tokens.add(new Token("DELIMITADOR", String.valueOf(c), linea));
            }

            else {
                tokens.add(new Token("ERROR", String.valueOf(c), linea));
            }
        }

        return tokens;
    }
}
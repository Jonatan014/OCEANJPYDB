package com.mycompany.prueba;
import java.util.ArrayList;
public class AnalizadorLexico {
    private ArrayList<Token> tokens = new ArrayList<>();
    private int linea = 1;
    public ArrayList<Token> analizar(String codigo) {
        tokens.clear();
        linea = 1;
        for (int i = 0; i < codigo.length(); i++) {
            char c = codigo.charAt(i);

            if (c == '\n') { linea++; continue; }
            if (Character.isWhitespace(c)) continue;

            // Comentarios --
            if (c == '-' && i + 1 < codigo.length() && codigo.charAt(i + 1) == '-') {
                while (i < codigo.length() && codigo.charAt(i) != '\n') i++;
                linea++;
                continue;
            }

            // Palabras reservadas e identificadores
            if (Character.isLetter(c)) {
                String lexema = "";
                while (i < codigo.length() && Character.isLetterOrDigit(codigo.charAt(i))) {
                    lexema += codigo.charAt(i);
                    i++;
                }
                i--;
                switch (lexema) {
                    case "trucha":
                    case "camaron":
                    case "salmon":
                        tokens.add(new Token("TIPO_DATO", lexema, linea));
                        break;
                    case "mostrar":
                    case "ancla":   // if
                    case "red":     // else
                        tokens.add(new Token("PALABRA_RESERVADA", lexema, linea));
                        break;
                    default:
                        tokens.add(new Token("IDENTIFICADOR", lexema, linea));
                }
                continue;
            }

            // Números
            if (Character.isDigit(c)) {
                String numero = "";
                boolean decimal = false;
                while (i < codigo.length()) {
                    char actual = codigo.charAt(i);
                    if (Character.isDigit(actual)) {
                        numero += actual;
                    } else if (actual == '.' && !decimal) {
                        decimal = true;
                        numero += actual;
                    } else break;
                    i++;
                }
                i--;
                tokens.add(new Token("NUMERO", numero, linea));
                continue;
            }

            // Strings
            if (c == '"') {
                String texto = "";
                i++;
                while (i < codigo.length() && codigo.charAt(i) != '"') {
                    texto += codigo.charAt(i);
                    i++;
                }
                tokens.add(new Token("STRING", "\"" + texto + "\"", linea));
                continue;
            }

            // Operadores de dos caracteres: ~~ == | !~ != | ~- <= | ~+ >=
            if (c == '~' && i + 1 < codigo.length()) {
                char sig = codigo.charAt(i + 1);
                if (sig == '~') { tokens.add(new Token("COMPARADOR", "~~", linea)); i++; continue; }
                if (sig == '-') { tokens.add(new Token("COMPARADOR", "~-", linea)); i++; continue; }
                if (sig == '+') { tokens.add(new Token("COMPARADOR", "~+", linea)); i++; continue; }
                // ~ solo → asignación
                tokens.add(new Token("OPERADOR", "~", linea)); continue;
            }
            if (c == '!' && i + 1 < codigo.length() && codigo.charAt(i + 1) == '~') {
                tokens.add(new Token("COMPARADOR", "!~", linea)); i++; continue;
            }

            // Operadores aritméticos / asignación (~ solo ya manejado arriba)
            if (c == '~') { tokens.add(new Token("OPERADOR", "~", linea)); continue; }
            if (c == '<') { tokens.add(new Token("OPERADOR", "<", linea)); continue; }
            if (c == '>') { tokens.add(new Token("OPERADOR", ">", linea)); continue; }
            if (c == '-') { tokens.add(new Token("COMPARADOR", "-", linea)); continue; }
            if (c == '+') { tokens.add(new Token("COMPARADOR", "+", linea)); continue; }
            if (c == '$') { tokens.add(new Token("OPERADOR", "$", linea)); continue; }
            if (c == '%') { tokens.add(new Token("OPERADOR", "%", linea)); continue; }

            // Delimitadores
            if (c == ';') { tokens.add(new Token("DELIMITADOR", ";", linea)); continue; }
            if (c == '(') { tokens.add(new Token("DELIMITADOR", "(", linea)); continue; }
            if (c == ')') { tokens.add(new Token("DELIMITADOR", ")", linea)); continue; }
            if (c == '{') { tokens.add(new Token("DELIMITADOR", "{", linea)); continue; }
            if (c == '}') { tokens.add(new Token("DELIMITADOR", "}", linea)); continue; }

            tokens.add(new Token("ERROR", String.valueOf(c), linea));
        }
        return tokens;
    }
}
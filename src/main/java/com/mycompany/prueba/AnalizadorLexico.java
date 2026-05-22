package com.mycompany.prueba;
 
import java.util.ArrayList;
 
public class AnalizadorLexico {
 
    private ArrayList<Token> tokens = new ArrayList<>();
    private int linea = 1;
 
    public ArrayList<Token> analizar(String codigo) throws ErrorLexico {
        tokens.clear();
        linea = 1;
 
        for (int i = 0; i < codigo.length(); i++) {
            char c = codigo.charAt(i);
 
            // Salto de línea
            if (c == '\n') { linea++; continue; }
            if (Character.isWhitespace(c)) continue;
 
            // Comentarios de una línea //
            if (c == '/' && i + 1 < codigo.length() && codigo.charAt(i + 1) == '/') {
                while (i < codigo.length() && codigo.charAt(i) != '\n') i++;
                linea++;
                continue;
            }
 
            // Palabras: tipos, reservadas, identificadores
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
                    case "ancla":    // if
                    case "red":      // else
                        tokens.add(new Token("PALABRA_RESERVADA", lexema, linea));
                        break;
                    default:
                        tokens.add(new Token("IDENTIFICADOR", lexema, linea));
                }
                continue;
            }
 
            // Números enteros y decimales
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
 
            // Strings entre comillas dobles
            if (c == '"') {
                String texto = "";
                i++;
                while (i < codigo.length() && codigo.charAt(i) != '"') {
                    texto += codigo.charAt(i);
                    i++;
                }
                tokens.add(new Token("STRING", texto, linea));
                continue;
            }
 
            // ── Operadores de dos caracteres (prefijo ~) ──────────────────────
            // ~~ (==)  ~- (<=)  ~+ (>=)   ~ solo → asignación
            if (c == '~') {
                if (i + 1 < codigo.length()) {
                    char sig = codigo.charAt(i + 1);
                    if (sig == '~') { tokens.add(new Token("COMPARADOR", "~~", linea)); i++; continue; }
                    if (sig == '-') { tokens.add(new Token("COMPARADOR", "~-", linea)); i++; continue; }
                    if (sig == '+') { tokens.add(new Token("COMPARADOR", "~+", linea)); i++; continue; }
                }
                tokens.add(new Token("OPERADOR", "~", linea));
                continue;
            }
 
            // !~ (!=)
            if (c == '!' && i + 1 < codigo.length() && codigo.charAt(i + 1) == '~') {
                tokens.add(new Token("COMPARADOR", "!~", linea)); i++; continue;
            }
 
            // ── Operadores aritméticos ────────────────────────────────────────
            if (c == '<') { tokens.add(new Token("OPERADOR",   "<", linea)); continue; }  // suma
            if (c == '>') { tokens.add(new Token("OPERADOR",   ">", linea)); continue; }  // resta
            if (c == '$') { tokens.add(new Token("OPERADOR",   "$", linea)); continue; }  // multiplicación
            if (c == '%') { tokens.add(new Token("OPERADOR",   "%", linea)); continue; }  // división
 
            // ── Comparadores simples ──────────────────────────────────────────
            if (c == '-') { tokens.add(new Token("COMPARADOR", "-", linea)); continue; }  // menor que
            if (c == '+') { tokens.add(new Token("COMPARADOR", "+", linea)); continue; }  // mayor que
 
            // ── Delimitadores ─────────────────────────────────────────────────
            if (c == ';') { tokens.add(new Token("DELIMITADOR", ";",            linea)); continue; }
            if (c == '(') { tokens.add(new Token("DELIMITADOR", "(",            linea)); continue; }
            if (c == ')') { tokens.add(new Token("DELIMITADOR", ")",            linea)); continue; }
            if (c == '{') { tokens.add(new Token("DELIMITADOR", "{",            linea)); continue; }
            if (c == '}') { tokens.add(new Token("DELIMITADOR", "}",            linea)); continue; }
 
            throw new ErrorLexico("Símbolo no reconocido '" + c + "' en linea " + linea);
        }
 
        return tokens;
    }
}
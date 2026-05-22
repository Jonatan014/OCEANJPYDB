package com.mycompany.prueba;

import java.util.ArrayList;

public class AnalizadorSemantico {

    private TablaSimbolos tabla;

    public String analizar(ArrayList<Token> tokens) throws ErrorSemantico {
        tabla = new TablaSimbolos();
        StringBuilder salida = new StringBuilder();
        analizarBloque(tokens, 0, tokens.size() - 1, salida);
        return salida.toString();
    }

    // ── Analiza tokens desde 'inicio' hasta 'fin' (inclusive) ────────────────
    private int analizarBloque(ArrayList<Token> tokens, int inicio, int fin,
                                StringBuilder salida) throws ErrorSemantico {
        int i = inicio;
        while (i <= fin) {
            Token t = tokens.get(i);

            // ── ancla (if) ───────────────────────────────────────────────────
            if (t.lexema.equals("ancla")) {
                i = procesarAncla(tokens, i, fin, salida);
                continue;
            }

            // ── Declaración de variable ──────────────────────────────────────
            if (t.tipo.equals("TIPO_DATO")) {
                i = procesarDeclaracion(tokens, i, salida);
                continue;
            }

            // ── mostrar ──────────────────────────────────────────────────────
            if (t.lexema.equals("mostrar")) {
                i = procesarMostrar(tokens, i, salida);
                continue;
            }

            // ── Token ignorable ──────────────────────────────────────────────
            if (t.tipo.equals("DELIMITADOR") || t.tipo.equals("PALABRA_RESERVADA")) {
                i++;
                continue;
            }

            throw new ErrorSemantico("Sentencia no válida en linea " + t.linea);
        }
        return i;
    }

    // ── Procesa ancla(cond) { ... } [red { ... }] ────────────────────────────
    private int procesarAncla(ArrayList<Token> tokens, int i, int fin,
                               StringBuilder salida) throws ErrorSemantico {
        Token t = tokens.get(i); // 'ancla'
        i++;

        // Esperar '('
        if (i > fin || !tokens.get(i).lexema.equals("("))
            throw new ErrorSemantico("Se esperaba '(' después de 'ancla' en linea " + t.linea);
        i++;

        // Recolectar tokens de la condición hasta ')'
        int inicioCondicion = i;
        int profundidad = 1;
        while (i <= fin && profundidad > 0) {
            if (tokens.get(i).lexema.equals("(")) profundidad++;
            else if (tokens.get(i).lexema.equals(")")) profundidad--;
            if (profundidad > 0) i++;
            else break;
        }
        int finCondicion = i - 1;
        i++; // saltar ')'

        boolean condicion = evaluarCondicion(tokens, inicioCondicion, finCondicion);

        // Esperar '{'
        if (i > fin || !tokens.get(i).lexema.equals("{"))
            throw new ErrorSemantico("Se esperaba '{' después de condición ancla en linea " + t.linea);
        i++;

        // Encontrar el '}' de cierre del bloque ancla
        int[] bloqueAncla = encontrarBloque(tokens, i, fin);
        int finBloqueAncla = bloqueAncla[0]; // índice del '}'

        if (condicion) {
            analizarBloque(tokens, i, finBloqueAncla - 1, salida);
        }
        i = finBloqueAncla + 1; // saltar '}'

        // ¿Hay un red (else)?
        if (i <= fin && tokens.get(i).lexema.equals("red")) {
            i++; // saltar 'red'
            if (i > fin || !tokens.get(i).lexema.equals("{"))
                throw new ErrorSemantico("Se esperaba '{' después de 'red' en linea " + t.linea);
            i++;

            int[] bloqueRed = encontrarBloque(tokens, i, fin);
            int finBloqueRed = bloqueRed[0];

            if (!condicion) {
                analizarBloque(tokens, i, finBloqueRed - 1, salida);
            }
            i = finBloqueRed + 1; // saltar '}'
        }

        return i;
    }

    // ── Devuelve el índice del '}' que cierra el bloque abierto en 'inicio' ──
    private int[] encontrarBloque(ArrayList<Token> tokens, int inicio, int fin) throws ErrorSemantico {
        int profundidad = 1;
        int i = inicio;
        while (i <= fin) {
            String lex = tokens.get(i).lexema;
            if (lex.equals("{")) profundidad++;
            else if (lex.equals("}")) {
                profundidad--;
                if (profundidad == 0) return new int[]{i};
            }
            i++;
        }
        throw new ErrorSemantico("Bloque sin cerrar '}'");
    }

    // ── Evalúa la condición: identificador COMPARADOR valor ──────────────────
    //    Comparadores: ?? (==)  !! (!=)  ?< (<=)  ?> (>=)
    private boolean evaluarCondicion(ArrayList<Token> tokens, int inicio, int fin)
            throws ErrorSemantico {

        // Buscar si hay algun COMPARADOR en el rango
        int posComp = -1;
        for (int i = inicio; i <= fin; i++) {
            if (tokens.get(i).tipo.equals("COMPARADOR")) {
                posComp = i;
                break;
            }
        }

        if (posComp != -1) {
            // Forma: expresion-izq COMPARADOR expresion-der
            Token compTok = tokens.get(posComp);
            Object izq = evaluarLado(tokens, inicio, posComp - 1);
            Object der = evaluarLado(tokens, posComp + 1, fin);
            return comparar(izq, der, compTok.lexema, compTok.linea);
        }

        // Sin comparador: evaluar expresion aritmetica, truthy si != 0 / no vacia
        Object resultado = evaluarLado(tokens, inicio, fin);
        if (resultado instanceof Integer) return ((Integer) resultado) != 0;
        if (resultado instanceof Double)  return ((Double)  resultado) != 0.0;
        if (resultado instanceof String) {
            String s = quitarComillas(resultado.toString());
            return !s.isEmpty();
        }

        throw new ErrorSemantico("Condicion invalida dentro de ancla en linea "
                + tokens.get(inicio).linea);
    }

    private Object evaluarLado(ArrayList<Token> tokens, int inicio, int fin)
            throws ErrorSemantico {
        if (inicio == fin) return obtenerValor(tokens.get(inicio));
        // Expresion aritmetica: usa SALMON para no perder decimales
        return evaluarExpresion(tokens, inicio, fin, TipoDato.SALMON);
    }

    private boolean comparar(Object izq, Object der, String op, int linea) throws ErrorSemantico {
        // Comparación de strings
        if (izq instanceof String && der instanceof String) {
            String si = quitarComillas(izq.toString());
            String sd = quitarComillas(der.toString());
            switch (op) {
                case "~~": return si.equals(sd);
                case "!~": return !si.equals(sd);
                default: throw new ErrorSemantico(
                    "Operador '" + op + "' no válido para texto en linea " + linea);
            }
        }
        // Comparación numérica
        double di = toDoubleObj(izq, op, linea);
        double dd = toDoubleObj(der, op, linea);
        switch (op) {
            case "~~": return di == dd;
            case "!~": return di != dd;
            case "~-": return di <= dd;
            case "~+": return di >= dd;
            case "-":  return di <  dd;
            case "+":  return di >  dd;
            default: throw new ErrorSemantico("Operador de comparacion desconocido: " + op);
        }
    }

    private double toDoubleObj(Object v, String op, int linea) throws ErrorSemantico {
        if (v instanceof Integer) return ((Integer) v).doubleValue();
        if (v instanceof Double)  return (Double) v;
        throw new ErrorSemantico("Operador '" + op + "' solo es válido entre números en linea " + linea);
    }

    // ── Procesa declaración de variable ──────────────────────────────────────
    private int procesarDeclaracion(ArrayList<Token> tokens, int i,
                                    StringBuilder salida) throws ErrorSemantico {
        Token t = tokens.get(i);
        try {
            Token identificador = tokens.get(i + 1);
            Token operador      = tokens.get(i + 2);
            Token valor         = tokens.get(i + 3);

            if (!identificador.tipo.equals("IDENTIFICADOR"))
                throw new ErrorSemantico("Se esperaba identificador en linea " + t.linea);
            if (!operador.lexema.equals("~"))
                throw new ErrorSemantico("Se esperaba '~' en linea " + t.linea);

            TipoDato tipo = obtenerTipo(t.lexema);
            if (tipo == null)
                throw new ErrorSemantico("Tipo de dato desconocido en linea " + t.linea);

            Object resultado;

            if (i + 4 < tokens.size() && tokens.get(i + 4).tipo.equals("OPERADOR")) {
                int inicioExp = i + 3;
                int finExp = inicioExp;
                while (finExp < tokens.size() && !tokens.get(finExp).lexema.equals(";")) finExp++;
                resultado = evaluarExpresion(tokens, inicioExp, finExp - 1, tipo);
                i = finExp;
            } else {
                Token fin = tokens.get(i + 4);
                if (!fin.lexema.equals(";"))
                    throw new ErrorSemantico("Se esperaba ';' en linea " + t.linea);
                resultado = validarValor(tipo, valor);
                i += 4;
            }

            tabla.agregar(new Variable(identificador.lexema, tipo, resultado));
            return i + 1;

        } catch (IndexOutOfBoundsException e) {
            throw new ErrorSemantico("Declaración incompleta en linea " + t.linea);
        }
    }

    // ── Procesa mostrar(var); ─────────────────────────────────────────────────
    private int procesarMostrar(ArrayList<Token> tokens, int i,
                                 StringBuilder salida) throws ErrorSemantico {
        Token t = tokens.get(i);
        try {
            Token abre   = tokens.get(i + 1);
            Token id     = tokens.get(i + 2);
            Token cierra = tokens.get(i + 3);
            Token fin    = tokens.get(i + 4);

            if (!abre.lexema.equals("("))
                throw new ErrorSemantico("Se esperaba '(' en linea " + t.linea);
            if (!id.tipo.equals("IDENTIFICADOR"))
                throw new ErrorSemantico("Se esperaba identificador en linea " + t.linea);
            if (!cierra.lexema.equals(")"))
                throw new ErrorSemantico("Se esperaba ')' en linea " + t.linea);
            if (!fin.lexema.equals(";"))
                throw new ErrorSemantico("Se esperaba ';' en linea " + t.linea);

            Variable var = tabla.obtener(id.lexema);
            if (var == null)
                throw new ErrorSemantico("Variable no declarada: " + id.lexema);

            salida.append(formatearValor(var)).append("\n");
            return i + 5;

        } catch (IndexOutOfBoundsException e) {
            throw new ErrorSemantico("Error en sentencia mostrar en linea " + t.linea);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String formatearValor(Variable var) {
        if (var.tipo == TipoDato.CAMARON)
            return String.valueOf(((Number) var.valor).intValue());
        if (var.tipo == TipoDato.SALMON) {
            double d = ((Number) var.valor).doubleValue();
            return String.format("%.8f", d).replaceAll("0+$", "").replaceAll("\\.$", ".0");
        }
        String s = var.valor.toString();
        if (s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length() - 1);
        return s;
    }

    private TipoDato obtenerTipo(String tipo) {
        switch (tipo) {
            case "trucha":  return TipoDato.TRUCHA;
            case "camaron": return TipoDato.CAMARON;
            case "salmon":  return TipoDato.SALMON;
            default:        return null;
        }
    }

    private Object validarValor(TipoDato tipo, Token valor) throws ErrorSemantico {
        switch (tipo) {
            case TRUCHA:
                if (!valor.tipo.equals("STRING"))
                    throw new ErrorSemantico("Error: trucha solo acepta texto (linea " + valor.linea + ")");
                return valor.lexema;
            case CAMARON:
                if (!valor.lexema.matches("-?\\d{1,10}"))
                    throw new ErrorSemantico(
                        "Error: camaron solo puede tener hasta 10 dígitos enteros (linea " + valor.linea + ")");
                try { return Integer.parseInt(valor.lexema); }
                catch (NumberFormatException e) {
                    throw new ErrorSemantico("Error: camaron solo acepta enteros válidos (linea " + valor.linea + ")");
                }
            case SALMON:
                if (!valor.lexema.matches("\\d{1,10}\\.\\d{1,8}"))
                    throw new ErrorSemantico(
                        "Error: salmon acepta formato decimal máximo 10 enteros y 8 decimales en linea " + valor.linea);
                try { return Double.parseDouble(valor.lexema); }
                catch (NumberFormatException e) {
                    throw new ErrorSemantico("Error: valor decimal inválido (linea " + valor.linea + ")");
                }
        }
        return null;
    }

    private int prioridad(String op) {
        switch (op) {
            case "$": case "%": return 2;
            case "<": case ">": return 1;
            default:            return 0;
        }
    }

    private Object evaluarExpresion(ArrayList<Token> tokens, int inicio, int fin, TipoDato tipo)
            throws ErrorSemantico {
        ArrayList<Object> valores    = new ArrayList<>();
        ArrayList<String> operadores = new ArrayList<>();

        for (int i = inicio; i <= fin; i++) {
            Token t = tokens.get(i);
            if (t.tipo.equals("NUMERO") || t.tipo.equals("IDENTIFICADOR") || t.tipo.equals("STRING")) {
                valores.add(obtenerValor(t));
            } else if (t.tipo.equals("OPERADOR")) {
                while (!operadores.isEmpty() &&
                       prioridad(operadores.get(operadores.size() - 1)) >= prioridad(t.lexema)) {
                    Object v2 = valores.remove(valores.size() - 1);
                    Object v1 = valores.remove(valores.size() - 1);
                    String op = operadores.remove(operadores.size() - 1);
                    valores.add(operar(v1, v2, op, tipo));
                }
                operadores.add(t.lexema);
            }
        }
        while (!operadores.isEmpty()) {
            Object v2 = valores.remove(valores.size() - 1);
            Object v1 = valores.remove(valores.size() - 1);
            String op = operadores.remove(operadores.size() - 1);
            valores.add(operar(v1, v2, op, tipo));
        }
        Object resultado = valores.get(0);
        if (tipo == TipoDato.CAMARON && resultado instanceof Double)
            return ((Double) resultado).intValue();
        return resultado;
    }

    private Object obtenerValor(Token t) throws ErrorSemantico {
        if (t.tipo.equals("NUMERO")) {
            String lex = t.lexema;
            if (!lex.contains(".")) return Integer.parseInt(lex);
            return Double.parseDouble(lex);
        }
        if (t.tipo.equals("STRING")) return t.lexema;
        if (t.tipo.equals("IDENTIFICADOR")) {
            Variable var = tabla.obtener(t.lexema);
            if (var == null) throw new ErrorSemantico("Variable no definida: " + t.lexema);
            return var.valor;
        }
        throw new ErrorSemantico("Valor no válido en linea " + t.linea);
    }

    private Object operar(Object v1, Object v2, String op, TipoDato tipoDestino) throws ErrorSemantico {
        if (op.equals("<")) {
            if (tipoDestino == TipoDato.TRUCHA) {
                return "\"" + quitarComillas(v1.toString()) + quitarComillas(v2.toString()) + "\"";
            }
            return toDouble(v1, op) + toDouble(v2, op);
        }
        switch (op) {
            case ">": return toDouble(v1, op) - toDouble(v2, op);
            case "$": return toDouble(v1, op) * toDouble(v2, op);
            case "%": {
                double d2 = toDouble(v2, op);
                return d2 != 0 ? toDouble(v1, op) / d2 : 0.0;
            }
        }
        throw new ErrorSemantico("Operador no válido: " + op);
    }

    private double toDouble(Object v, String op) throws ErrorSemantico {
        if (v instanceof Integer) return ((Integer) v).doubleValue();
        if (v instanceof Double)  return (Double) v;
        throw new ErrorSemantico("Operación '" + op + "' solo es válida entre números");
    }

    private String quitarComillas(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) return s.substring(1, s.length() - 1);
        return s;
    }
}
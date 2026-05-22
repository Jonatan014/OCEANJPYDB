/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prueba;

/**
 *
 * @author jonat
 */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class VentanaPrincipal extends JFrame {

    private JTextArea txtCodigo;
    private JTable tablaTokens;
    private JTextArea txtResultados;
    private String arbolGenerado = "";

    private AnalizadorLexico al = new AnalizadorLexico();
    private AnalizadorSemantico as = new AnalizadorSemantico();

    public VentanaPrincipal() {
        setTitle("OceanJpy++#DBjsNativeShell on rails");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelEntrada = new JPanel(new BorderLayout());
        panelEntrada.setBorder(BorderFactory.createTitledBorder("Código"));

        txtCodigo = new JTextArea();
        JScrollPane scrollCodigo = new JScrollPane(txtCodigo);

        JButton btnAnalizar = new JButton("Analizar");
        JButton btnArbol = new JButton("Mostrar Producción");

        panelEntrada.add(scrollCodigo, BorderLayout.CENTER);
       JPanel panelBotones = new JPanel();

        panelBotones.add(btnAnalizar);
        panelBotones.add(btnArbol);

        panelEntrada.add(panelBotones, BorderLayout.SOUTH);

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Tabla de Tokens"));

        tablaTokens = new JTable();
        JScrollPane scrollTabla = new JScrollPane(tablaTokens);

        panelTabla.add(scrollTabla, BorderLayout.CENTER);
        JTextArea txtAyuda = new JTextArea();
        txtAyuda.setEditable(false);
        txtAyuda.setFont(new Font("Monospaced", Font.PLAIN, 12));

        txtAyuda.setText("""
        === SIMBOLOGÍA DEL LENGUAJE ===

        TIPOS DE DATO:
        trucha  → String (texto)
        camaron → int (número entero)
        salmon  → double (decimal)

        OPERADORES:
        <  → suma (+)
        >  → resta (-)
        ~  → asignación (=)
        $  → multiplicación (*)
        %  → división (/)

        PALABRA RESERVADA:
        mostrar → imprime el valor de una variable

        EJEMPLO:
        salmon p1 ~ 5.0;
        salmon p2 ~ 3.0;
        salmon r ~ p1 < p2;
        mostrar(r);
        """);

        JScrollPane scrollAyuda = new JScrollPane(txtAyuda);
        scrollAyuda.setBorder(BorderFactory.createTitledBorder("Ayuda / Lenguaje"));
        scrollAyuda.setPreferredSize(new Dimension(400, 150));

        panelTabla.add(scrollAyuda, BorderLayout.SOUTH);

        JPanel panelResultados = new JPanel(new BorderLayout());
        panelResultados.setBorder(BorderFactory.createTitledBorder("Resultados"));

        txtResultados = new JTextArea();
        txtResultados.setEditable(false);
        JScrollPane scrollResultados = new JScrollPane(txtResultados);

        panelResultados.add(scrollResultados, BorderLayout.CENTER);

        JSplitPane splitSuperior = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelEntrada, panelTabla);
        splitSuperior.setDividerLocation(400);

        add(splitSuperior, BorderLayout.CENTER);
        add(panelResultados, BorderLayout.SOUTH);
        panelResultados.setPreferredSize(new Dimension(800, 150));

        btnAnalizar.addActionListener(e -> analizarCodigo());
        btnArbol.addActionListener(e -> mostrarProduccion());

        setVisible(true);
    }

    private void analizarCodigo() {
        String codigo = txtCodigo.getText();

        try {
            ArrayList<Token> tokens = al.analizar(codigo);

            DefaultTableModel modelo = new DefaultTableModel();
            modelo.addColumn("Tipo");
            modelo.addColumn("Lexema");
            modelo.addColumn("Patrón");
            modelo.addColumn("Reservada");

            for (Token t : tokens) {
                modelo.addRow(new Object[]{
                    t.tipo,
                    t.lexema,
                    obtenerPatron(t),
                    esReservada(t)
                });
            }

            tablaTokens.setModel(modelo);

            String resultado = as.analizar(tokens);

            arbolGenerado = generarArbol(tokens);
            txtResultados.setText(resultado.isEmpty() ? "Análisis correcto" : resultado);

        } catch (ErrorLexico ex) {

    txtResultados.setText(
        "ERROR LÉXICO\n\n" +
        ex.getMessage()
    );
}

catch (ErrorSintactico ex) {

    txtResultados.setText(
        "ERROR SINTÁCTICO\n\n" +
        ex.getMessage()
    );
}

catch (ErrorSemantico ex) {

    txtResultados.setText(
        "ERROR SEMÁNTICO\n\n" +
        ex.getMessage()
    );
}

catch (ErrorLogico ex) {

    txtResultados.setText(
        "ERROR LÓGICO\n\n" +
        ex.getMessage()
    );
}

catch (Exception ex) {

    txtResultados.setText(
        "ERROR GENERAL\n\n" +
        ex.getMessage()
    );
}
    }
    
    private void mostrarProduccion() {

    if (arbolGenerado.isEmpty()) {
        JOptionPane.showMessageDialog(
            this,
            "Primero analiza un código",
            "Información",
            JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }

    JTextArea area = new JTextArea(arbolGenerado);
    area.setEditable(false);
    area.setFont(new Font("Monospaced", Font.PLAIN, 14));

    JScrollPane scroll = new JScrollPane(area);
    scroll.setPreferredSize(new Dimension(500, 400));

    JOptionPane.showMessageDialog(
        this,
        scroll,
        "Producción / Árbol Sintáctico",
        JOptionPane.INFORMATION_MESSAGE
    );
 }
    
    private String generarProduccion(ArrayList<Token> tokens) {

    StringBuilder sb = new StringBuilder();

    sb.append("=== PRODUCCIÓN SINTÁCTICA ===\n\n");

    for (int i = 0; i < tokens.size(); i++) {

        Token t = tokens.get(i);

        if (t.tipo.equals("TIPO_DATO")) {

            sb.append("<declaracion> → ");
            sb.append("<tipo_dato> ");
            sb.append("<identificador> ");
            sb.append("<asignacion> ");
            sb.append("<expresion> ");
            sb.append(";\n");

            sb.append("   ")
              .append(t.lexema)
              .append(" → tipo_dato\n");

            if (i + 1 < tokens.size()) {
                sb.append("   ")
                  .append(tokens.get(i + 1).lexema)
                  .append(" → identificador\n");
            }

            if (i + 2 < tokens.size()) {
                sb.append("   ")
                  .append(tokens.get(i + 2).lexema)
                  .append(" → asignacion\n");
            }
        }

        else if (t.lexema.equals("mostrar")) {

            sb.append("\n<mostrar> → mostrar ( identificador ) ;\n");

            sb.append("   mostrar → palabra_reservada\n");

            if (i + 2 < tokens.size()) {
                sb.append("   ")
                  .append(tokens.get(i + 2).lexema)
                  .append(" → identificador\n");
            }
        }
    }

    return sb.toString();
}
    
    private String obtenerPatron(Token t) {

    switch (t.tipo) {

        case "TIPO_DATO":

            switch (t.lexema) {

                case "trucha":
                    return "Texto entre comillas";

                case "camaron":
                    return "Entero máximo 10 dígitos";

                case "salmon":
                    return "Decimal obligatorio (#.#) máximo 8 caracteres";
            }

            break;

        case "IDENTIFICADOR":
            return "[a-zA-Z][a-zA-Z0-9]{0,9}";

        case "NUMERO":
            return """
            camaron: \\d{1,10}
            salmon: \\d{1,10}\\.\\d{1,8}
            """;

        case "STRING":
            return "\"texto\"";

        case "OPERADOR":

            switch (t.lexema) {

                case "<":
                    return "Suma / Concatenación";

                case ">":
                    return "Resta";

                case "~":
                    return "Asignación";

                case "$":
                    return "Multiplicación";

                case "%":
                    return "División";
            }

            break;

        case "DELIMITADOR":

            switch (t.lexema) {

                case ";":
                    return "Fin de instrucción";

                case "(":
                    return "Inicio agrupación";

                case ")":
                    return "Fin agrupación";
            }

            break;

        case "PALABRA_RESERVADA":
            return "mostrar(variable);";

        case "ERROR":
            return "Token inválido";
    }

    return "Desconocido";
}
    private String esReservada(Token t) {
        if (t.tipo.equals("PALABRA_RESERVADA") ||
            t.tipo.equals("TIPO_DATO") ||
            t.tipo.equals("OPERADOR") ||
            t.tipo.equals("DELIMITADOR")) {

            return "Sí";
        }
        return "No";
    }
    private String generarArbol(ArrayList<Token> tokens) {

    StringBuilder arbol = new StringBuilder();

    for (int i = 0; i < tokens.size(); i++) {

        Token t = tokens.get(i);

        // declaración
        if (t.tipo.equals("IDENTIFICADOR")) {

            arbol.append("<declaracion>\n");

            arbol.append(" ├── <identificador> → ")
                  .append(t.lexema)
                  .append("\n");

            if (i + 1 < tokens.size()) {

                arbol.append(" ├── <tipo_dato> → ")
                      .append(tokens.get(i + 1).lexema)
                      .append("\n");
            }

            if (i + 2 < tokens.size()) {

                arbol.append(" ├── <asignacion> → ")
                      .append(tokens.get(i + 2).lexema)
                      .append("\n");
            }

            arbol.append(" ├── <expresion>\n");

            int j = i + 3;

            while (j < tokens.size() &&
                   !tokens.get(j).lexema.equals(";")) {

                Token aux = tokens.get(j);

                switch (aux.tipo) {

                    case "NUMERO":

                        arbol.append(" │     ├── <numero> → ")
                              .append(aux.lexema)
                              .append("\n");
                        break;

                    case "STRING":

                        arbol.append(" │     ├── <string> → \"")
                              .append(aux.lexema)
                              .append("\"\n");
                        break;

                    case "IDENTIFICADOR":

                        arbol.append(" │     ├── <identificador> → ")
                              .append(aux.lexema)
                              .append("\n");
                        break;

                    case "OPERADOR":

                        arbol.append(" │     ├── <operador> → ")
                              .append(aux.lexema)
                              .append("\n");
                        break;
                }

                j++;
            }

            arbol.append(" └── <delimitador> → ;\n\n");
        }

        // mostrar()
        else if (t.lexema.equals("mostrar")) {

            arbol.append("<mostrar>\n");

            arbol.append(" ├── palabra_reservada → mostrar\n");

            if (i + 2 < tokens.size()) {

                arbol.append(" └── identificador → ")
                      .append(tokens.get(i + 2).lexema)
                      .append("\n\n");
            }
        }
    }

    return arbol.toString();
    }
}
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

    private AnalizadorLexico al = new AnalizadorLexico();
    private AnalizadorSemantico as = new AnalizadorSemantico();

    public VentanaPrincipal() {
        setTitle("Analizador Léxico y Semántico");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 🔹 PANEL 1 (Entrada)
        JPanel panelEntrada = new JPanel(new BorderLayout());
        panelEntrada.setBorder(BorderFactory.createTitledBorder("Código"));

        txtCodigo = new JTextArea();
        JScrollPane scrollCodigo = new JScrollPane(txtCodigo);

        JButton btnAnalizar = new JButton("Analizar");

        panelEntrada.add(scrollCodigo, BorderLayout.CENTER);
        panelEntrada.add(btnAnalizar, BorderLayout.SOUTH);

        // 🔹 PANEL 2 (Tabla de tokens)
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Tabla de Tokens"));

        tablaTokens = new JTable();
        JScrollPane scrollTabla = new JScrollPane(tablaTokens);

        panelTabla.add(scrollTabla, BorderLayout.CENTER);

        // 🔹 PANEL 3 (Resultados)
        JPanel panelResultados = new JPanel(new BorderLayout());
        panelResultados.setBorder(BorderFactory.createTitledBorder("Resultados"));

        txtResultados = new JTextArea();
        txtResultados.setEditable(false);
        JScrollPane scrollResultados = new JScrollPane(txtResultados);

        panelResultados.add(scrollResultados, BorderLayout.CENTER);

        // 🔹 Panel superior (divide entrada y tabla)
        JSplitPane splitSuperior = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelEntrada, panelTabla);
        splitSuperior.setDividerLocation(400);

        // 🔹 Panel principal
        add(splitSuperior, BorderLayout.CENTER);
        add(panelResultados, BorderLayout.SOUTH);
        panelResultados.setPreferredSize(new Dimension(800, 150));

        // 🔥 EVENTO BOTÓN
        btnAnalizar.addActionListener(e -> analizarCodigo());

        setVisible(true);
    }

    private void analizarCodigo() {
        String codigo = txtCodigo.getText();

        try {
            // 🔹 Análisis léxico
            ArrayList<Token> tokens = al.analizar(codigo);

            // 🔹 Llenar tabla
            DefaultTableModel modelo = new DefaultTableModel();
            modelo.addColumn("Tipo");
            modelo.addColumn("Lexema");
            modelo.addColumn("Línea");

            for (Token t : tokens) {
                modelo.addRow(new Object[]{t.tipo, t.lexema, t.linea});
            }

            tablaTokens.setModel(modelo);

            // 🔹 Análisis semántico
            String resultado = as.analizar(tokens);
txtResultados.setText(resultado.isEmpty() ? "Análisis correcto" : resultado);

        } catch (ErrorSemantico ex) {
            txtResultados.setText(ex.getMessage());
        }
    }
}
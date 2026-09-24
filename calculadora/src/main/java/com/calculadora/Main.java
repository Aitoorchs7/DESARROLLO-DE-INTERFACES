package com.calculadora;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            CalculadoraCientifica calculadora = new CalculadoraCientifica();
            calculadora.setVisible(true);
        });
    }
}
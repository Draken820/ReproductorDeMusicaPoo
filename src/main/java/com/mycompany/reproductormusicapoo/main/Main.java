package com.mycompany.reproductormusicapoo.main;

import com.formdev.flatlaf.FlatDarkLaf;
import com.mycompany.reproductormusicapoo.view.ReproductorGUI;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

public class Main {
    public static void main(String[] args) {
        try {
            FlatDarkLaf.setup();
            UIManager.put("Button.arc", 999); 
            UIManager.put("Component.arc", 20); 
            UIManager.put("Slider.thumbWidth", 15);
            UIManager.put("Slider.trackHeight", 5);
            UIManager.put("Slider.thumbColor", Color.WHITE);
            UIManager.put("Slider.trackSelectedColor", new Color(255, 255, 255, 200));
            UIManager.put("Slider.trackColor", new Color(255, 255, 255, 60));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new ReproductorGUI().setVisible(true);
        });
    }
}
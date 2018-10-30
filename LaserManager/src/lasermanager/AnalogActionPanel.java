/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lasermanager;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Color;

/**
 *
 * @author user
 */
public class AnalogActionPanel extends JPanel {

    private final Color ActiveBackground;
    private final Color InactiveBackground;
    private final JButton ActionButton;
    public AnalogAction Action = null;
    
    public ActionActionListener ActionListener = null;

    public AnalogActionPanel(ActionActionListener listener) {
        super(new BorderLayout(5, 5));
        
        this.ActionListener = listener;

        ActiveBackground = new Color(164, 197, 153);
        InactiveBackground = new Color(174, 177, 193);

        setOpaque(false);
        ImageIcon icon = createImageIcon("images/nochange.png");
        ActionButton = new JButton("", icon);
        ActionButton.setHorizontalAlignment(SwingConstants.LEFT);
        ActionButton.setBackground(InactiveBackground);
        ActionButton.getModel().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ActionListener != null) {
                    ActionListener.ActionPerformed(Action.Index);
                }
            }
        });

        this.add(ActionButton);
    }

    protected ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, "");
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public void updateValue(AnalogAction value) {
        if (value != null) {
            Action = value;
        }

        if (Action == null) {
            return;
        }

        ImageIcon icon;
        switch (Action.ActionType) {
            case AnalogAction.ACTION_NONE:
                ActionButton.setBackground(InactiveBackground);
                icon = createImageIcon("images/nochange.png");
                ActionButton.setIcon(icon);
                break;
            case AnalogAction.ACTION_CHANGE:
                ActionButton.setBackground(ActiveBackground);
                icon = createImageIcon("images/change.png");
                ActionButton.setIcon(icon);
                break;
        }

        ActionButton.setText(GetValueText());
    }

    private String GetValueText() {
        switch (Action.ValueType) {
            case AnalogAction.VALUE_VOLTAGE:
                return (String.format("%.2f", Action.Value) + " V");
            case AnalogAction.VALUE_WATTAGE:
                return (String.format("%.1f", Action.Value) + " mW");
        }
        return "";
    }
}

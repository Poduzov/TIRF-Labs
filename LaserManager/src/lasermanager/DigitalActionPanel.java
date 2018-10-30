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

/**
 *
 * @author user
 */
public class DigitalActionPanel extends JPanel {

    private final JButton ActionButton;
    public DigitalAction Action = null;
    public ActionActionListener ActionListener = null;

    public DigitalActionPanel(ActionActionListener listener) {
        super(new BorderLayout(5, 5));
        
        this.ActionListener = listener;
        setOpaque(false);
        ImageIcon icon = createImageIcon("images/nochange.png");
        ActionButton = new JButton("", icon);
        ActionButton.setHorizontalAlignment(SwingConstants.LEFT);
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

    public void updateValue(DigitalAction value) {
        if (value != null) {
            Action = value;
        }

        if (Action == null) {
            return;
        }

        ImageIcon icon;
        switch (Action.ActionType) {
            case DigitalAction.ACTION_ON:
                icon = createImageIcon("images/rising.png");
                ActionButton.setIcon(icon);
                ActionButton.setText("On");
                break;
            case DigitalAction.ACTION_OFF:
                icon = createImageIcon("images/falling.png");
                ActionButton.setText("Off");
                ActionButton.setIcon(icon);
                break;
            case DigitalAction.ACTION_NONE:
                icon = createImageIcon("images/nochange.png");
                ActionButton.setText("None");
                ActionButton.setIcon(icon);
                break;
        }

    }
}

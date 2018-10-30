/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lasermanager;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author user
 */
public class WattagePanel extends JPanel {

    private static final int DEFAULT = 0;
    public final JSlider slider = new JSlider(0, 100);
    public final JSpinner spinner = new JSpinner();

    public WattagePanel() {
        super(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        spinner.setModel(new SpinnerNumberModel(0.0d, 0.0d, 2000.0d, 0.1d));
        spinner.setOpaque(false);
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.0"));
        spinner.setValue(DEFAULT);
        slider.setOpaque(false);
        slider.setFocusable(false);
        slider.setValue(DEFAULT);
        slider.setMaximum(20000);
        slider.setMinimum(0);

        slider.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                BoundedRangeModel m = (BoundedRangeModel) e.getSource();
                spinner.setValue((double) m.getValue() / 10.0d);
            }
        });

        spinner.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                SpinnerNumberModel m = (SpinnerNumberModel) e.getSource();
                slider.setValue((int) Math.round((Double) m.getValue() * 10.0d));
            }
        });

        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        JPanel pSpinner = new JPanel(new BorderLayout(0, 0));
        pSpinner.setOpaque(false);
        pSpinner.add(spinner, BorderLayout.CENTER);
        pSpinner.add(new JLabel("  mW  "), BorderLayout.AFTER_LINE_ENDS);
        p.add(pSpinner, BorderLayout.PAGE_START);
        p.add(slider, BorderLayout.PAGE_END);
        add(p, BorderLayout.CENTER);
    }

    public void updateValue(Double value) {
        slider.setValue((int) Math.round(value * 10.0d));
        spinner.setValue(value);
    }
}

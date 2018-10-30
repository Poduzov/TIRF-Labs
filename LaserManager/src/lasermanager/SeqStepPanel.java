/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lasermanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class SeqStepPanel extends JPanel {

    private final JButton TriggerButton;
    private final JTable ActionsTable;
    private final JLabel IndexLabel;
    private final JButton DeleteButton;
    private final JButton AddButton;
    private final JButton RunButton;

    public SequenceStepActionListener ActionListener = null;
    private final AnalogActionListener AnalogListener = new AnalogActionListener();
    private final DigitalActionListener DigitalListener = new DigitalActionListener();

    SequenceStep Step = null;

    public SeqStepPanel() {
        super(new BorderLayout(5, 5));

        JPanel p1, p2, p3;
        ImageIcon icon;

        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        icon = createImageIcon("images/usb.png");
        TriggerButton = new JButton("200ms", icon);
        TriggerButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
        TriggerButton.setMinimumSize(new Dimension(130, 40));
        TriggerButton.setPreferredSize(new Dimension(130, 40));
        TriggerButton.setHorizontalAlignment(SwingConstants.LEFT);
        TriggerButton.setIconTextGap(10);
        TriggerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SequenceStepActionParam param = new SequenceStepActionParam();
                param.EventType = SequenceStepActionParam.EVENT_EDIT_TRIGGER;

                if (ActionListener != null) {
                    ActionListener.ActionPerformed(param);
                }
            }
        });

        ActionsTable = new JTable();
        ActionsTable.setOpaque(false);
        ActionsTable.setRowHeight(40);
        DefaultTableModel model = (DefaultTableModel) ActionsTable.getModel();
        String ids[] = new String[Configuration.AO_COUNT + Configuration.DO_COUNT];
        for (int i = 0; i < Configuration.AO_COUNT; i++) {
            ids[i] = "AO " + String.valueOf(i + 1);
        }
        for (int i = 0; i < Configuration.DO_COUNT; i++) {
            ids[i + Configuration.AO_COUNT] = "DO " + String.valueOf(i + 1);
        }
        model.setColumnIdentifiers(ids);
        Object data[] = new Object[Configuration.AO_COUNT + Configuration.DO_COUNT];
        for (int i = 0; i < Configuration.AO_COUNT + Configuration.DO_COUNT; i++) {
            data[i] = i;
        }
        model.addRow(data);
        ActionsTable.getTableHeader().setReorderingAllowed(false);
        ActionsTable.getTableHeader().setResizingAllowed(false);

        for (int i = 0; i < Configuration.AO_COUNT; i++) {
            ActionsTable.getColumnModel().getColumn(i).setCellEditor(new AnalogActionEditor(AnalogListener));
            ActionsTable.getColumnModel().getColumn(i).setCellRenderer(new AnalogActionRenderer(AnalogListener));
        }

        for (int i = Configuration.AO_COUNT; i < Configuration.AO_COUNT + Configuration.DO_COUNT; i++) {
            ActionsTable.getColumnModel().getColumn(i).setCellEditor(new DigitalActionEditor(DigitalListener));
            ActionsTable.getColumnModel().getColumn(i).setCellRenderer(new DigitalActionRenderer(DigitalListener));
        }

        IndexLabel = new JLabel("");
        IndexLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        IndexLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
        IndexLabel.setMaximumSize(new Dimension(30, 100));
        IndexLabel.setPreferredSize(new Dimension(30, 100));
        IndexLabel.setOpaque(false);

        JPanel TablePanel = new JPanel();
        TablePanel.setLayout(new BorderLayout());
        TablePanel.add(ActionsTable, BorderLayout.CENTER);
        TablePanel.add(ActionsTable.getTableHeader(), BorderLayout.PAGE_START);

        this.add(IndexLabel, BorderLayout.WEST);

        p1 = new JPanel();
        p1.setOpaque(false);
        p1.setLayout(new BorderLayout());

        p2 = new JPanel();
        p2.setBorder(new EmptyBorder(0, 0, 2, 0));
        p2.setOpaque(false);
        p2.setLayout(new BorderLayout());
        p2.add(TriggerButton, BorderLayout.WEST);

        p3 = new JPanel();
        p3.setOpaque(false);
        p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));

        
        
        icon = createImageIcon("images/play.png");
        RunButton = new JButton("", icon);
        RunButton.setAlignmentX(RIGHT_ALIGNMENT);
        RunButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SequenceStepActionParam param = new SequenceStepActionParam();
                param.EventType = SequenceStepActionParam.EVENT_START;

                if (ActionListener != null) {
                    ActionListener.ActionPerformed(param);
                }
            }
        });
        p3.add(RunButton);
        
        p3.add(new Filler(new java.awt.Dimension(30, 0), new java.awt.Dimension(30, 0), new java.awt.Dimension(32767, 0)));

        icon = createImageIcon("images/add.png");
        AddButton = new JButton("", icon);
        AddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SequenceStepActionParam param = new SequenceStepActionParam();
                param.EventType = SequenceStepActionParam.EVENT_ADD;

                if (ActionListener != null) {
                    ActionListener.ActionPerformed(param);
                }
            }
        });
        p3.add(AddButton);

        icon = createImageIcon("images/delete.png");
        DeleteButton = new JButton("", icon);
        DeleteButton.setAlignmentX(RIGHT_ALIGNMENT);
        DeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SequenceStepActionParam param = new SequenceStepActionParam();
                param.EventType = SequenceStepActionParam.EVENT_DELETE;

                if (ActionListener != null) {
                    ActionListener.ActionPerformed(param);
                }
            }
        });
        p3.add(DeleteButton);

        p2.add(p3, BorderLayout.EAST);

        p1.add(p2, BorderLayout.PAGE_START);
        p1.add(TablePanel, BorderLayout.CENTER);

        this.add(p1, BorderLayout.CENTER);

    }

    public void updateValue(SequenceStep value) {
        if (value != null) {
            Step = value;
        }

        if (Step == null) {
            return;
        }

        ImageIcon icon;
        switch (Step.Trigger.Type) {
            case SequenceTrigger.TRIGGER_RISING_1:
                icon = createImageIcon("images/rising.png");
                TriggerButton.setIcon(icon);
                TriggerButton.setText("DI 1");
                break;
            case SequenceTrigger.TRIGGER_FALLING_1:
                icon = createImageIcon("images/falling.png");
                TriggerButton.setIcon(icon);
                TriggerButton.setText("DI 1");
                break;
            case SequenceTrigger.TRIGGER_RISING_2:
                icon = createImageIcon("images/rising.png");
                TriggerButton.setIcon(icon);
                TriggerButton.setText("DI 2");
                break;
            case SequenceTrigger.TRIGGER_FALLING_2:
                icon = createImageIcon("images/falling.png");
                TriggerButton.setIcon(icon);
                TriggerButton.setText("DI 2");
                break;
            case SequenceTrigger.TRIGGER_TIMER:
                icon = createImageIcon("images/timer.png");
                TriggerButton.setIcon(icon);
                TriggerButton.setText(String.valueOf(Step.Trigger.TimerValue / 10.0f) + " s");
                break;
            case SequenceTrigger.TRIGGER_USB:
                icon = createImageIcon("images/usb.png");
                TriggerButton.setIcon(icon);
                TriggerButton.setText("USB");
                break;
        }

        DefaultTableModel m = (DefaultTableModel) ActionsTable.getModel();
        for (int i = 0; i < Step.AnalogActions.length; i++) {
            m.setValueAt(Step.AnalogActions[i], 0, i);
            ((AnalogActionRenderer) ActionsTable.getColumnModel().getColumn(i).getCellRenderer()).updateValue(Step.AnalogActions[i]);
            ((AnalogActionEditor) ActionsTable.getColumnModel().getColumn(i).getCellEditor()).updateValue(Step.AnalogActions[i]);
        }

        for (int i = 0; i < Step.DigitalActions.length; i++) {
            m.setValueAt(Step.DigitalActions[i], 0, i + Configuration.AO_COUNT);
            ((DigitalActionRenderer) ActionsTable.getColumnModel().getColumn(i + Configuration.AO_COUNT).getCellRenderer()).updateValue(Step.DigitalActions[i]);
            ((DigitalActionEditor) ActionsTable.getColumnModel().getColumn(i + Configuration.AO_COUNT).getCellEditor()).updateValue(Step.DigitalActions[i]);
        }

        IndexLabel.setText(String.valueOf(Step.Index));
        m.fireTableDataChanged();
    }

    public void updateSelected(boolean isSelected) {
        IndexLabel.setForeground(isSelected ? Color.WHITE : Color.BLACK);
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

    private class AnalogActionListener implements ActionActionListener {

        @Override
        public void ActionPerformed(int index) {
            SequenceStepActionParam param = new SequenceStepActionParam();
            param.EventType = SequenceStepActionParam.EVENT_EDIT_ANALOG_ACTION;
            param.ActionIndex = index;

            if (ActionListener != null) {
                ActionListener.ActionPerformed(param);
            }
        }
    }

    private class DigitalActionListener implements ActionActionListener {

        @Override
        public void ActionPerformed(int index) {
            SequenceStepActionParam param = new SequenceStepActionParam();
            param.EventType = SequenceStepActionParam.EVENT_EDIT_DIGITAL_ACTION;
            param.ActionIndex = index;

            if (ActionListener != null) {
                ActionListener.ActionPerformed(param);
            }
        }
    }
}

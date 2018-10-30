/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lasermanager;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author user
 */
public class SeqStepRenderer extends SeqStepPanel implements TableCellRenderer {

    public SeqStepRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        updateSelected(isSelected);
        if (value instanceof SequenceStep) {
            updateValue((SequenceStep) value);
        }
        return this;
    }
}

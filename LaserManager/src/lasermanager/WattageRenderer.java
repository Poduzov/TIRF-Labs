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
public class WattageRenderer extends WattagePanel implements TableCellRenderer {

    public WattageRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        if (value instanceof Double) {
            updateValue((Double) value);
        }
        return this;
    }
}

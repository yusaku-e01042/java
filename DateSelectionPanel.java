import javax.swing.*;
import java.awt.*;

public class DateSelectionPanel extends JPanel {
    private JComboBox<String> comboYear, comboMonth, comboDay;

    public DateSelectionPanel(int startYear, int endYear) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        comboYear = new JComboBox<>();
        for (int i = startYear; i <= endYear; i++) comboYear.addItem(String.valueOf(i));
        comboMonth = new JComboBox<>();
        for (int i = 1; i <= 12; i++) comboMonth.addItem(String.format("%02d", i));
        comboDay = new JComboBox<>();
        for (int i = 1; i <= 31; i++) comboDay.addItem(String.format("%02d", i));
        add(comboYear); add(new JLabel("年 "));
        add(comboMonth); add(new JLabel("月 "));
        add(comboDay); add(new JLabel("日"));
    }

    public String getDateString() {
        return comboYear.getSelectedItem() + "/" + comboMonth.getSelectedItem() + "/" + comboDay.getSelectedItem();
    }

    public void setDateString(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || !dateStr.contains("/")) return;
        String[] parts = dateStr.split("/");
        if (parts.length >= 3) {
            comboYear.setSelectedItem(parts[0]);
            comboMonth.setSelectedItem(parts[1]);
            comboDay.setSelectedItem(parts[2]);
        }
    }
}
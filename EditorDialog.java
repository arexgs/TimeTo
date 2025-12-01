package todolist;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date; 

public class EditorDialog extends JDialog {
    private static final Color MAIN_BG = new Color(245, 247, 252);
    private static final Color HEADER_BG = new Color(138, 180, 248);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Font DEFAULT_FONT = new Font("Serif", Font.PLAIN, 14); // Font standar
    private static final Font BOLD_FONT = DEFAULT_FONT.deriveFont(Font.BOLD); // Font tebal untuk judul/tombol
    private static final Font HEADER_BTN_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private JTextField titleField;
    private JTextArea descField;
    private JSpinner dateSpinner;
    private JCheckBox doneCheckbox;
    private int rowIndex = -1;
    private boolean isNewTask = true;

    public EditorDialog(JFrame parent, DefaultTableModel model, int editingRow) {
        super(parent, "Task Editor", true);
        this.isNewTask = editingRow == -1;
        this.rowIndex = editingRow;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(500, 350); 
        setLocationRelativeTo(parent);
        getContentPane().setBackground(MAIN_BG);

        //Untuk Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel(isNewTask ? "New Task" : "Edit Task");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(HEADER_BTN_FONT.deriveFont(Font.BOLD, 16f));
        headerPanel.add(titleLabel);

        //Untuk Panel utama
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(MAIN_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        //Untuk input Title
        titleField = new JTextField(20);
        titleField.setFont(DEFAULT_FONT); 
        mainPanel.add(createLabeledField("Title:", titleField));
        mainPanel.add(Box.createVerticalStrut(10));

        //Untuk deskripsi
        descField = new JTextArea(4, 20);
        descField.setFont(DEFAULT_FONT); 
        descField.setLineWrap(true);
        descField.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descField);
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(DEFAULT_FONT);
        mainPanel.add(descLabel);
        mainPanel.add(descScroll);
        mainPanel.add(Box.createVerticalStrut(10));

        //Untuk deadline
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateModel.setValue(new java.util.Date());
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateEditor.getTextField().setFont(DEFAULT_FONT);
        mainPanel.add(createLabeledField("Deadline:", dateSpinner));
        mainPanel.add(Box.createVerticalStrut(10));

        //Untuk menambah tombol centang Done
        doneCheckbox = new JCheckBox("Mark as Done");
        doneCheckbox.setFont(DEFAULT_FONT); 
        doneCheckbox.setBackground(MAIN_BG);
        mainPanel.add(doneCheckbox);
        mainPanel.add(Box.createVerticalStrut(10));

        //Untuk tombol Save dan Cancel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(MAIN_BG);

        JButton saveBtn = createStyledButton("Save", new Color(100, 200, 100));
        saveBtn.addActionListener(e -> saveTask(model));
        
        JButton cancelBtn = createStyledButton("Cancel", new Color(200, 100, 100));
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        if (!isNewTask) {
            loadTaskData(model);
        }
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(MAIN_BG);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(DEFAULT_FONT); 
        lbl.setPreferredSize(new Dimension(100, 25));
        panel.add(lbl, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(80, 30));
        btn.setFont(BOLD_FONT); 
        return btn;
    }
    
    private void loadTaskData(DefaultTableModel model) {
        if (rowIndex >= 0 && rowIndex < model.getRowCount()) {
            doneCheckbox.setSelected((Boolean) model.getValueAt(rowIndex, 0));
            titleField.setText((String) model.getValueAt(rowIndex, 1));
            descField.setText((String) model.getValueAt(rowIndex, 2));
            String dlStr = (String) model.getValueAt(rowIndex, 3);
            if (dlStr != null && !dlStr.isEmpty()) {
                try {
                    LocalDate dl = LocalDate.parse(dlStr, DATE_FMT);
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.set(dl.getYear(), dl.getMonthValue() - 1, dl.getDayOfMonth());
                    dateSpinner.setValue(cal.getTime());
                } catch (Exception ex) {}
            }
        }
    }

    private void saveTask(DefaultTableModel model) {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String desc = descField.getText();
        LocalDate deadline = extractDateFromSpinner();
        boolean done = doneCheckbox.isSelected();

        if (isNewTask) {
            model.addRow(new Object[]{done, title, desc, deadline.format(DATE_FMT)});
        } else if (rowIndex >= 0) {
            model.setValueAt(done, rowIndex, 0);
            model.setValueAt(title, rowIndex, 1);
            model.setValueAt(desc, rowIndex, 2);
            model.setValueAt(deadline.format(DATE_FMT), rowIndex, 3);
        }

        dispose();
    }

    private LocalDate extractDateFromSpinner() {
        java.util.Date date = (java.util.Date) dateSpinner.getValue();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        return LocalDate.of(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.DAY_OF_MONTH));
    }
}
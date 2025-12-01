package TimeTo;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.RowSorter.SortKey;
import javax.swing.Timer;

public class Main extends JFrame {
    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel statusLabel;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private int lastOverdueCount = 0;
    private JLayeredPane layered;
    private SnackbarNotification snackbar;
    private TableRenderer tableRenderer;
    private CalendarPanel calendarPanel;

    private static final Color MAIN_BG = new Color(245, 247, 252);
    private static final Color SIDEBAR_BG = new Color(232, 237, 247);
    private static final Color HEADER_BG = new Color(138, 180, 248);
    private static final Color TABLE_EVEN = new Color(255, 255, 255);
    private static final Color TABLE_ODD = new Color(248, 250, 254);
    private static final Color SELECTED_ROW = new Color(173, 216, 230);
    private static final Color DONE_BG = new Color(230, 230, 240);
    
    private static final Color UPCOMING_GREEN = new Color(200, 242, 195);
    private static final Color WARNING_YELLOW = new Color(255, 245, 179);
    private static final Color OVERDUE_RED = new Color(255, 205, 210);
    
    private static final Color BTN_ADD = new Color(165, 214, 255);
    private static final Color BTN_EDIT = new Color(200, 230, 201);
    private static final Color BTN_DELETE = new Color(255, 171, 145);
    private static final Color BTN_CLEAR = new Color(206, 195, 255);
    
    private static final Color STATUS_NORMAL = new Color(240, 242, 245);
    private static final Color STATUS_WARNING = new Color(255, 238, 238);

    public Main() {
        super("TIME TO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(MAIN_BG);

        model = new DefaultTableModel(new Object[]{"Done", "Title", "Description", "Deadline"}, 0) {
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return String.class;
            }
            @Override public boolean isCellEditable(int row, int col) { return col == 0; }
        };

        table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(SELECTED_ROW);
                    c.setForeground(Color.BLACK);
                    return c;
                }
                int mRow = convertRowIndexToModel(row);
                boolean done = Boolean.TRUE.equals(model.getValueAt(mRow, 0));
                int daysLeft = Integer.MAX_VALUE;
                boolean overdue = false;
                try {
                    Object dl = model.getValueAt(mRow, 3);
                    if (dl != null && !dl.toString().trim().isEmpty()) {
                        LocalDate d = LocalDate.parse(dl.toString(), DATE_FMT);
                        daysLeft = (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), d);
                        overdue = daysLeft < 0;
                    }
                } catch (Exception ex) {}
                
                Color bg = (row % 2 == 0) ? TABLE_EVEN : TABLE_ODD;
                if (done) c.setBackground(DONE_BG);
                else if (overdue) c.setBackground(OVERDUE_RED);
                else if (daysLeft <= 2) c.setBackground(WARNING_YELLOW);
                else if (daysLeft <= 7) c.setBackground(UPCOMING_GREEN);
                else c.setBackground(bg);
                return c;
            }
        };
        table.setFillsViewportHeight(true);
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,0));

        sorter = new TableRowSorter<>(model);
        sorter.setComparator(3, (o1,o2) -> {
            try {
                if (o1 == null || o1.toString().trim().isEmpty()) return (o2 == null || o2.toString().trim().isEmpty()) ? 0 : 1;
                if (o2 == null || o2.toString().trim().isEmpty()) return -1;
                LocalDate d1 = LocalDate.parse(o1.toString(), DATE_FMT);
                LocalDate d2 = LocalDate.parse(o2.toString(), DATE_FMT);
                return d1.compareTo(d2); 
            } catch (Exception ex) { return String.valueOf(o1).compareTo(String.valueOf(o2)); }
        });
        table.setRowSorter(sorter);

        List<SortKey> keys = new ArrayList<>();
        keys.add(new SortKey(3, SortOrder.ASCENDING));
        sorter.setSortKeys(keys);

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13f));
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(getFont().deriveFont(Font.BOLD, 13f));
                setBackground(HEADER_BG);
                setForeground(Color.WHITE);
                return c;
            }
        };
        
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        tableRenderer = new TableRenderer(model);
        table.setDefaultRenderer(Object.class, tableRenderer);
        table.setDefaultRenderer(String.class, tableRenderer);

        TableColumnModel tcm = table.getColumnModel();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        add(scroll, BorderLayout.CENTER);

        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JPanel controlsPanel = new JPanel();
        controlsPanel.setOpaque(false);
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        
        ImageIcon rawIcon = new ImageIcon(getClass().getResource("assets/logo.png"));
        Image logoScaledImg = rawIcon.getImage().getScaledInstance(48, 72, Image.SCALE_SMOOTH);
        ImageIcon appIcon = new ImageIcon(logoScaledImg);
        JLabel appTitle = new JLabel(appIcon);
        appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        appTitle.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        controlsPanel.add(appTitle);
        controlsPanel.add(Box.createVerticalStrut(12));

        searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(180,26));
        searchField.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchField.setToolTipText("Search title/description");
        controlsPanel.add(searchField);
        controlsPanel.add(Box.createVerticalStrut(8));

        filterCombo = new JComboBox<>(new String[]{"All","Overdue","Upcoming in 7 days","Done"});
        filterCombo.setMaximumSize(new Dimension(180,28));
        filterCombo.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlsPanel.add(filterCombo);
        controlsPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        
        JButton addBtn = modernButton("⊕ Add Task", BTN_ADD);
        JButton editBtn = modernButton("✑ Edit Selected", BTN_EDIT);
        JButton deleteBtn = modernButton("⊗ Delete Selected", BTN_DELETE);
        JButton clearBtn = modernButton("⟳ Clear Finished", BTN_CLEAR);
        
        JButton calendarBtn = new JButton();
        try {
            Image calendarIcon = ImageIO.read(getClass().getResource("assets/CalendarIcon.jpeg"));
            Image calendarScaledImg = calendarIcon.getScaledInstance(170, 170, Image.SCALE_SMOOTH);
            calendarBtn.setIcon(new ImageIcon(calendarScaledImg));
            calendarBtn.setContentAreaFilled(false);
            calendarBtn.setBorderPainted(false);
            calendarBtn.setFocusPainted(false);
        } catch (Exception e) {
            calendarBtn.setText("@ Calendar");
        }
        calendarBtn.setToolTipText("Open Calendar");
        
        for (JButton b : Arrays.asList(addBtn, editBtn, deleteBtn, clearBtn)) {
            b.setMaximumSize(new Dimension(180,36));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel.add(b);
            buttonPanel.add(Box.createVerticalStrut(10));
        }
        
        calendarBtn.setMaximumSize(new Dimension(180,36));
        calendarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(calendarBtn);
        buttonPanel.add(Box.createVerticalStrut(10));

        buttonPanel.add(Box.createVerticalGlue());
        sidebar.add(controlsPanel, BorderLayout.NORTH);
        sidebar.add(buttonPanel, BorderLayout.CENTER);
        add(sidebar, BorderLayout.WEST);

        statusLabel = new JLabel(" Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(STATUS_NORMAL);
        statusLabel.setForeground(new Color(60, 60, 70));
        add(statusLabel, BorderLayout.SOUTH);

        layered = getLayeredPane();
        snackbar = new SnackbarNotification(this);

        addBtn.addActionListener(e -> showEditor(false, -1));
        editBtn.addActionListener(e -> { int vr = table.getSelectedRow(); if (vr<0) { snackbar.show("Pilih task untuk edit", 2000); return;} showEditor(true, table.convertRowIndexToModel(vr)); });
        deleteBtn.addActionListener(e -> deleteSelectedTask());
        clearBtn.addActionListener(e -> clearFinishedTasks());
        calendarBtn.addActionListener(e -> showCalendarDialog());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); } public void removeUpdate(DocumentEvent e) { applyFilters(); } public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        filterCombo.addActionListener(e -> applyFilters());
        model.addTableModelListener(e -> SwingUtilities.invokeLater(() -> { sorter.sort(); table.repaint(); updateStatus(); })); 

        addRowWithoutRank(false, "Projek PBO", "Lengkapi GUI", LocalDate.now().plusDays(2));
        addRowWithoutRank(false, "Beli kebutuhan", "Susu dan roti", LocalDate.now().plusDays(5));
        addRowWithoutRank(true, "Responsi 1 PBO", "Membuat program dengan memuat semua materi", LocalDate.now().minusDays(3));
        addRowWithoutRank(false, "Olahraga", "Lari pagi selama 30 menit", LocalDate.now().minusDays(5));

        new javax.swing.Timer(60_000, ev -> checkReminders()).start();

        sorter.sort();
        updateStatus();
    }

    private JButton modernButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(new Color(40, 40, 60));
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1), 
            BorderFactory.createEmptyBorder(8,12,8,12)
        ));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { 
                b.setBackground(bg.darker()); 
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 
            }
            public void mouseExited(java.awt.event.MouseEvent e) { 
                b.setBackground(bg); 
            }
        });
        return b;
    }

    private void showEditor(boolean isEdit, int modelRow) {
        EditorDialog editorDialog = new EditorDialog(this, model, modelRow);
        editorDialog.setVisible(true);
        sorter.sort();
        updateStatus();
    }

    private void addRowWithoutRank(boolean done, String title, String desc, LocalDate dl) {
        String sdl = dl==null? "": dl.format(DATE_FMT);
        model.addRow(new Object[]{done, title, desc, sdl}); 
    }

    private void applyFilters() {
        String text = searchField.getText().trim();
        String filter = (String) filterCombo.getSelectedItem();
        List<RowFilter<DefaultTableModel,Object>> filters = new ArrayList<>();
        if (!text.isEmpty()) {
            try { filters.add(RowFilter.regexFilter("(?i)"+Pattern.quote(text), 1,2)); } catch(Exception ex){}
        }
        
        if ("Upcoming in 7 days".equals(filter)) {
            filters.add(new RowFilter<DefaultTableModel,Object>(){ public boolean include(Entry<? extends DefaultTableModel, ? extends Object> e) {
                try { 
                    Boolean done = (Boolean)e.getValue(0);
                    String dlStr = (String)e.getValue(3);
                    if (done) return false;
                    if (dlStr==null || dlStr.trim().isEmpty()) return false;
                    LocalDate d = LocalDate.parse(dlStr, DATE_FMT);
                    long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), d);
                    return daysLeft >= 0 && daysLeft <= 7;
                } catch(Exception ex){return false;}
            }});
        } else if ("Overdue".equals(filter)) {
            filters.add(new RowFilter<DefaultTableModel,Object>(){ public boolean include(Entry<? extends DefaultTableModel, ? extends Object> e) {
                try { 
                    Boolean done = (Boolean)e.getValue(0);
                    String dlStr = (String)e.getValue(3);
                    if (done) return false;
                    if (dlStr==null || dlStr.trim().isEmpty()) return false;
                    LocalDate d = LocalDate.parse(dlStr, DATE_FMT);
                    return d.isBefore(LocalDate.now());
                } catch(Exception ex){return false;}
            }});
        } else if ("Done".equals(filter)) {
            filters.add(new RowFilter<DefaultTableModel,Object>(){ public boolean include(Entry<? extends DefaultTableModel, ? extends Object> e) {
                try { Boolean done = (Boolean)e.getValue(0); return done!=null && done; } catch(Exception ex){return false;}
            }});
        }
        
        if (filters.isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.andFilter(filters));
    }

    private void deleteSelectedTask() {
        int vr = table.getSelectedRow();
        if (vr<0) { snackbar.show("Pilih task untuk dihapus", 2000); return; }
        int mr = table.convertRowIndexToModel(vr);
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus task terpilih?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm==JOptionPane.YES_OPTION) { model.removeRow(mr); snackbar.show("Task dihapus", 2000); }
    }

    private void clearFinishedTasks() {
        boolean removed = false;
        for (int i=model.getRowCount()-1;i>=0;i--) {
            Boolean d = (Boolean) model.getValueAt(i,0);
            if (d!=null && d) { model.removeRow(i); removed=true; }
        }
        if (removed) snackbar.show("Finished tasks cleared", 2000);
    }

    private void updateStatus() {
        int total = model.getRowCount(), done=0, overdue=0;
        for (int i=0;i<total;i++) {
            Boolean d = (Boolean) model.getValueAt(i,0);
            if (d!=null && d) done++;
            try {
                String dl = (String) model.getValueAt(i,3);
                if ((d==null || !d) && dl!=null && !dl.isEmpty()) {
                    LocalDate date = LocalDate.parse(dl, DATE_FMT);
                    if (date.isBefore(LocalDate.now())) overdue++;
                }
            } catch(Exception ex){}
        }
        statusLabel.setText(" Total: "+total+"    Done: "+done+"    Overdue: "+overdue);
        statusLabel.setBackground(overdue>0? STATUS_WARNING : STATUS_NORMAL);
    }

    private void checkReminders() {
        int overdue=0;
        for (int i=0;i<model.getRowCount();i++) {
            Boolean d = (Boolean) model.getValueAt(i,0);
            String dl = (String) model.getValueAt(i,3);
            try {
                if ((d==null || !d) && dl!=null && !dl.isEmpty()) {
                    LocalDate date = LocalDate.parse(dl, DATE_FMT);
                    if (date.isBefore(LocalDate.now())) overdue++;
                }
            } catch(Exception ex){}
        }
        if (overdue>lastOverdueCount && overdue>0) {
            final int c = overdue;
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Ada "+c+" task yang lewat deadline!", "Reminder", JOptionPane.WARNING_MESSAGE));
        }
        lastOverdueCount = overdue;
        updateStatus();
    }

    private void showCalendarDialog() {
        JDialog calendarDialog = new JDialog(this, "Calendar - View Tasks by Date", true);
        calendarDialog.setSize(700, 600);
        calendarDialog.setLocationRelativeTo(this);
        
        calendarPanel = new CalendarPanel(model);
        calendarDialog.add(calendarPanel, BorderLayout.CENTER);
        calendarDialog.setVisible(true);
    }

    private static String escapeHtml(String s) {
        if (s==null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
            catch (Exception ex) { }
            
            Main gui = new Main();
            gui.setVisible(true);
        });
    }
}
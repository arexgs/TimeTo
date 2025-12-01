package TimeTo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

public class CalendarPanel extends JPanel {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DefaultTableModel model;
    private JTextArea taskDetailsArea;
    
    private static final Color PANEL_BG = new Color(245, 247, 252);
    private static final Color HEADER_BG = new Color(138, 180, 248);
    private static final Color DAY_HEADER_BG = new Color(173, 216, 230);
    private static final Color DAY_HEADER_FG = new Color(40, 80, 150);
    
    //Untuk penanda status tugas pada kalender
    private static final Color DONE_BG = new Color(230, 230, 240);       //Abu-abu muda ketika sudah selesai
    private static final Color OVERDUE_BG = new Color(255, 205, 210);    //Merah muda ketik terlambat
    private static final Color WARNING_BG = new Color(255, 245, 179);    //Kuning untuk deadline task yang sudah dekat
    private static final Color TASK_DAY_BG = new Color(200, 242, 195);   //Hijau untuk task yang deadline-nya masih agak lama
    private static final Color NORMAL_DAY_BG = new Color(255, 255, 255);  // Putih untuk hari biasa
    private static final Color TODAY_BG = new Color(255, 248, 225);      //Cream cerah untuk pendanda hari ini
    private static final Color TODAY_BORDER = new Color(255, 152, 0);    //Border orange untuk hari ini
    private static final Color HOVER_BG = new Color(224, 247, 250);

    public CalendarPanel(DefaultTableModel model) {
        this.model = model;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(PANEL_BG);
        initComponents();
    }

    private void initComponents() {
        LocalDate displayMonth = LocalDate.now().withDayOfMonth(1);
        
        //Untuk header kalender
        JLabel monthYearLabel = new JLabel();
        monthYearLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        monthYearLabel.setHorizontalAlignment(JLabel.CENTER);
        monthYearLabel.setForeground(Color.WHITE);

        //Untuk tombol navigasi pada bulan
        JButton prevMonthBtn = createNavButton("Prev");
        JButton nextMonthBtn = createNavButton("Next");
        
        JPanel navPanel = new JPanel(new BorderLayout(10, 0));
        navPanel.setOpaque(false);
        navPanel.add(prevMonthBtn, BorderLayout.WEST);
        navPanel.add(monthYearLabel, BorderLayout.CENTER);
        navPanel.add(nextMonthBtn, BorderLayout.EAST);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        headerPanel.add(navPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        //Untuk grid kalender
        JPanel calendarGridPanel = new JPanel();
        calendarGridPanel.setLayout(new BorderLayout());
        calendarGridPanel.setBackground(PANEL_BG);
        add(calendarGridPanel, BorderLayout.CENTER);

        //Untuk detail tasks
        taskDetailsArea = new JTextArea(6, 50);
        taskDetailsArea.setEditable(false);
        taskDetailsArea.setLineWrap(true);
        taskDetailsArea.setWrapStyleWord(true);
        taskDetailsArea.setFont(new Font("Dialog", Font.PLAIN, 14));
        taskDetailsArea.setBackground(new Color(255, 255, 255));
        
        JScrollPane taskScroll = new JScrollPane(taskDetailsArea);
        taskScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(138, 180, 248), 2),
            "Task Details",
            0,
            0,
            new Font("Segoe UI", Font.BOLD, 13),
            new Color(60, 90, 180)
        ));
        add(taskScroll, BorderLayout.SOUTH);

        //Untuk tampilan kalender
        Consumer<LocalDate> updateCalendar = (month) -> {
            calendarGridPanel.removeAll();
            monthYearLabel.setText(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

            JPanel gridPanel = new JPanel(new GridLayout(7, 7, 3, 3));
            gridPanel.setBackground(PANEL_BG);
            gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            //Untuk header hari
            String[] dayHeaders = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (String day : dayHeaders) {
                JLabel dayLabel = new JLabel(day);
                dayLabel.setHorizontalAlignment(JLabel.CENTER);
                dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                dayLabel.setForeground(DAY_HEADER_FG);
                dayLabel.setBackground(DAY_HEADER_BG);
                dayLabel.setOpaque(true);
                dayLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
                gridPanel.add(dayLabel);
            }

            LocalDate firstDay = month.withDayOfMonth(1);
            int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
            int daysInMonth = month.lengthOfMonth();

            //Untuk sel kosong sebelum hari pertama bulan
            for (int i = 0; i < firstDayOfWeek; i++) {
                JPanel emptyPanel = new JPanel();
                emptyPanel.setBackground(PANEL_BG);
                gridPanel.add(emptyPanel);
            }

            LocalDate today = LocalDate.now();

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate dateOfDay = month.withDayOfMonth(day);
                
                //Untuk menghitung status tugas pada hari tersebut
                int tasksCount = 0;
                boolean anyDone = false;
                boolean allDone = true;
                boolean anyOverdue = false;
                boolean anyNear = false;

                for (int i = 0; i < model.getRowCount(); i++) {
                    String dlStr = (String) model.getValueAt(i, 3);
                    if (dlStr != null && !dlStr.isEmpty()) {
                        try {
                            LocalDate dl = LocalDate.parse(dlStr, DATE_FMT);
                            if (dl.equals(dateOfDay)) {
                                tasksCount++;
                                Boolean done = (Boolean) model.getValueAt(i, 0);
                                if (done) anyDone = true;
                                else allDone = false;

                                if (!done && dl.isBefore(today)) anyOverdue = true;
                                
                                long daysLeft = ChronoUnit.DAYS.between(today, dl);
                                if (!done && daysLeft >= 0 && daysLeft <= 2) anyNear = true;
                            }
                        } catch (Exception ex) {}
                    }
                }
                if (tasksCount == 0) allDone = false;

                final int finalTasksCount = tasksCount;

                JPanel dayPanel = new JPanel(new BorderLayout());
                
                Color cellBg = NORMAL_DAY_BG;
                if (finalTasksCount > 0) {
                    if (allDone) cellBg = DONE_BG;
                    else if (anyOverdue) cellBg = OVERDUE_BG;
                    else if (anyNear) cellBg = WARNING_BG;
                    else cellBg = TASK_DAY_BG;
                }
                
                if (dateOfDay.equals(today)) {
                    cellBg = TODAY_BG;
                }
                dayPanel.setBackground(cellBg);

                if (dateOfDay.equals(today)) {
                    dayPanel.setBorder(BorderFactory.createLineBorder(TODAY_BORDER, 2));
                } else if (finalTasksCount > 0) {
                    dayPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                } else {
                    dayPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
                }

                //UNtuk label tanggal
                JLabel dateLabel = new JLabel(String.valueOf(day));
                dateLabel.setHorizontalAlignment(JLabel.CENTER);
                dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                dateLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
                
                //Untuk warna tanggal
                if (dateOfDay.equals(today)) dateLabel.setForeground(new Color(230, 81, 0));
                else if (finalTasksCount > 0) dateLabel.setForeground(Color.DARK_GRAY);
                else dateLabel.setForeground(Color.GRAY);

                //Untuk menambahkan label tanggal dan indikator tugas
                JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                topRow.setOpaque(false);
                topRow.add(dateLabel);
                
                //Untuk menambah "!"" jika ada tugas mendesak
                if (finalTasksCount > 0 && anyNear && !allDone) {
                    JLabel warn = new JLabel(" !");
                    warn.setForeground(Color.RED.darker());
                    warn.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    topRow.add(warn);
                }
                dayPanel.add(topRow, BorderLayout.NORTH);

                //Untuk menambahkan jumlah tugas pada hari tersebut
                if (finalTasksCount > 0) {
                    JLabel taskCountLabel = new JLabel(finalTasksCount + " task" + (finalTasksCount > 1 ? "s" : ""));
                    taskCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    taskCountLabel.setHorizontalAlignment(JLabel.CENTER);
                    
                    if (allDone) {
                        taskCountLabel.setForeground(Color.GRAY);
                    } else {
                        taskCountLabel.setForeground(new Color(46, 125, 50));
                    }
                    
                    dayPanel.add(taskCountLabel, BorderLayout.SOUTH);
                }

                LocalDate finalDateOfDay = dateOfDay;
                Color finalBg = cellBg;
                
                dayPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        taskDetailsArea.setText(getTasksForDate(finalDateOfDay));
                    }
                    
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        dayPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        if (!finalDateOfDay.equals(today)) {
                            dayPanel.setBackground(HOVER_BG);
                        }
                        dayPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
                    }
                    
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        dayPanel.setBackground(finalBg);

                        if (finalDateOfDay.equals(today)) {
                            dayPanel.setBorder(BorderFactory.createLineBorder(TODAY_BORDER, 2));
                        } else {
                             dayPanel.setBorder(BorderFactory.createLineBorder(
                                 finalTasksCount > 0 ? Color.LIGHT_GRAY : new Color(230, 230, 230), 1));
                        }
                    }
                });

                gridPanel.add(dayPanel);
            }

            int totalCells = firstDayOfWeek + daysInMonth;
            int remainingCells = 42 - totalCells;
            for (int i = 0; i < remainingCells; i++) {
                JPanel emptyPanel = new JPanel();
                emptyPanel.setBackground(PANEL_BG);
                gridPanel.add(emptyPanel);
            }

            calendarGridPanel.add(gridPanel, BorderLayout.CENTER);
            calendarGridPanel.revalidate();
            calendarGridPanel.repaint();
        };

        LocalDate[] currentMonth = {displayMonth};
        updateCalendar.accept(currentMonth[0]);

        prevMonthBtn.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].minusMonths(1);
            updateCalendar.accept(currentMonth[0]);
        });

        nextMonthBtn.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].plusMonths(1);
            updateCalendar.accept(currentMonth[0]);
        });
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(100, 150, 230));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(80, 130, 210));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(100, 150, 230));
            }
        });
        return btn;
    }


    public String getTasksForDate(LocalDate date) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tasks for ").append(date.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy"))).append("\n");
        sb.append(" --------------------------------------------------\n\n");

        int count = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String dlStr = (String) model.getValueAt(i, 3);
            if (dlStr != null && !dlStr.isEmpty()) {
                try {
                    LocalDate dl = LocalDate.parse(dlStr, DATE_FMT);
                    if (dl.equals(date)) {
                        Boolean done = (Boolean) model.getValueAt(i, 0);
                        String title = (String) model.getValueAt(i, 1);
                        String desc = (String) model.getValueAt(i, 2);
                        
                        count++;
                        
                        sb.append(count).append(". ");
                        sb.append(done ? "[✓] " : "[✗]");
                        sb.append(title);
                        
                        if (desc != null && !desc.isEmpty()) {
                            String indentedDesc = desc.trim().replaceAll("\\n", "\n     | ");
                            sb.append("\n     | ").append(indentedDesc);
                        }
                        
                        sb.append("\n\n");
                    }
                } catch (Exception ex) {}
            }
        }

        if (count == 0) {
            sb.append("No tasks scheduled for this date.");
        }

        return sb.toString();
    }
}

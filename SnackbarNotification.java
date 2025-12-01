package todolist;

import javax.swing.*;
import java.awt.*;

public class SnackbarNotification {
    private final JFrame parent;
    private JLabel snackbar;

    public SnackbarNotification(JFrame parent) {
        this.parent = parent;
        initSnackbar();
    }

    private void initSnackbar() {
        snackbar = new JLabel();
        snackbar.setBackground(new Color(50, 50, 50));
        snackbar.setForeground(Color.WHITE);
        snackbar.setOpaque(true);
        snackbar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        snackbar.setFont(snackbar.getFont().deriveFont(java.awt.Font.PLAIN, 12f));
        snackbar.setHorizontalAlignment(JLabel.LEFT);
        snackbar.setVisible(false);

        JLayeredPane layeredPane = parent.getLayeredPane();
        layeredPane.add(snackbar, JLayeredPane.POPUP_LAYER);
    }

    public void show(String message, int durationMs) {
        snackbar.setText(message);

        Dimension parentSize = parent.getSize();
        int width = 300;
        int height = 40;
        int x = parentSize.width - width - 15;
        int y = parentSize.height - height - 70;
        snackbar.setBounds(x, y, width, height);
        snackbar.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            Timer timer = new Timer(durationMs, e -> snackbar.setVisible(false));
            timer.setRepeats(false);
            timer.start();
        });
    }
}
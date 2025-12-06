package cybersecuritydashboard;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class MatrixBackgroundPanel extends JPanel {

    private String[] rows;
    private Random random = new Random();

    public MatrixBackgroundPanel() {
        setOpaque(true);
        setBackground(new Color(5, 5, 10));
        rows = new String[20];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = randomRow();
        }

        // simple animation timer (updates every 150 ms)
        new Timer(150, e -> {
            // shift rows down and create new random row at top
            for (int i = rows.length - 1; i > 0; i--) {
                rows[i] = rows[i - 1];
            }
            rows[0] = randomRow();
            repaint();
        }).start();
    }

    private String randomRow() {
        int len = 40;
        String symbols = "01ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(symbols.charAt(random.nextInt(symbols.length())));
        }
        return sb.toString();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(new Color(0, 200, 0));
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));

        int lineHeight = g2.getFontMetrics().getHeight();
        int y = lineHeight;
        for (String row : rows) {
            g2.drawString(row, 10, y);
            y += lineHeight;
            if (y > getHeight()) break;
        }
        g2.dispose();
    }
}

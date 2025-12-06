package cybersecuritydashboard;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class PasswordPanel extends JPanel {

    private CyberSecurityDashBoard parent;

    private JPasswordField passwordField;
    private JLabel resultLabel;
    private JTextArea detailsArea;
    private JButton checkButton;

    // strong password: digit, lower, upper, special, 8–20 chars, no spaces
    private static final Pattern STRONG_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$"
    );

    public PasswordPanel(CyberSecurityDashBoard parent) {
        this.parent = parent;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(15, 15, 20));

        // top bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(new Color(25, 25, 35));

        JLabel title = new JLabel("Password Strength Checker");
        title.setForeground(new Color(180, 120, 255));
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        top.add(title);

        top.add(new JLabel("Password:"));
        passwordField = new JPasswordField(20);
        passwordField.setBackground(new Color(35, 35, 45));
        passwordField.setForeground(Color.WHITE);
        top.add(passwordField);

        checkButton = new JButton("Check strength");
        top.add(checkButton);

        add(top, BorderLayout.NORTH);

        // center area
        resultLabel = new JLabel("Strength: ");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultLabel.setForeground(Color.WHITE);

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setBackground(new Color(10, 10, 15));
        detailsArea.setForeground(new Color(0, 255, 128));      // green text
        detailsArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        detailsArea.setMargin(new Insets(8, 8, 8, 8));

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(new Color(15, 15, 20));
        center.add(resultLabel, BorderLayout.NORTH);
        center.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        checkButton.addActionListener(e -> evaluatePassword());
    }

    private void evaluatePassword() {
        String pwd = new String(passwordField.getPassword());
        detailsArea.setText("");

        if (pwd.length() < 8) {
            resultLabel.setText("Strength: WEAK");
            resultLabel.setForeground(new Color(255, 80, 80));
            detailsArea.append("Password too short (minimum 8 characters).\n");
            return;
        }

        int score = 0;
        if (pwd.matches(".*[0-9].*")) score++;
        if (pwd.matches(".*[a-z].*")) score++;
        if (pwd.matches(".*[A-Z].*")) score++;
        if (pwd.matches(".*[@#$%^&+=].*")) score++;
        if (!pwd.matches(".*\\s.*")) score++;

        if (STRONG_PATTERN.matcher(pwd).matches()) {
            resultLabel.setText("Strength: STRONG");
            resultLabel.setForeground(new Color(0, 255, 128));      // green
            parent.totalStrongPasswords++;
        } else if (score >= 3) {
            resultLabel.setText("Strength: MEDIUM");
            resultLabel.setForeground(new Color(255, 215, 0));      // yellow
        } else {
            resultLabel.setText("Strength: WEAK");
            resultLabel.setForeground(new Color(255, 80, 80));      // red
        }

        if (!pwd.matches(".*[0-9].*")) detailsArea.append("Add at least one digit.\n");
        if (!pwd.matches(".*[a-z].*")) detailsArea.append("Add at least one lowercase letter.\n");
        if (!pwd.matches(".*[A-Z].*")) detailsArea.append("Add at least one uppercase letter.\n");
        if (!pwd.matches(".*[@#$%^&+=].*")) detailsArea.append("Add at least one special character (@#$%^&+=).\n");
        if (pwd.matches(".*\\s.*")) detailsArea.append("Remove spaces from the password.\n");
        if (pwd.length() > 20) detailsArea.append("Use at most 20 characters.\n");
    }
}

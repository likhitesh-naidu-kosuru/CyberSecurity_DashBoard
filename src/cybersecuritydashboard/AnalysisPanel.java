package cybersecuritydashboard;

import javax.swing.*;
import java.awt.*;

public class AnalysisPanel extends JPanel {

    private CyberSecurityDashBoard parent;
    private JLabel hostsLabel, portsLabel, filesLabel, pwdLabel;

    public AnalysisPanel(CyberSecurityDashBoard parent) {
        this.parent = parent;

        setLayout(new GridLayout(4, 1, 15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        setBackground(new Color(15, 15, 20));

        hostsLabel = createCardLabel();
        portsLabel = createCardLabel();
        filesLabel = createCardLabel();
        pwdLabel   = createCardLabel();

        add(hostsLabel);
        add(portsLabel);
        add(filesLabel);
        add(pwdLabel);

        refresh();
    }

    private JLabel createCardLabel() {
    JLabel l = new JLabel("", SwingConstants.CENTER);
    l.setOpaque(true);
    l.setBackground(new Color(40, 40, 70));                 // lighter card
    l.setForeground(new Color(255, 255, 255));
    l.setFont(new Font("Segoe UI", Font.BOLD, 22));
    l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(90, 90, 130), 2),   // border
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
    ));
    return l;
    }


    public void refresh() {
        hostsLabel.setText("Active hosts found: " + parent.totalActiveHosts);
        portsLabel.setText("Open ports detected: " + parent.totalOpenPorts);
        filesLabel.setText("Files encrypted: " + parent.totalFilesEncrypted);
        pwdLabel.setText("Strong passwords checked: " + parent.totalStrongPasswords);
    }
}

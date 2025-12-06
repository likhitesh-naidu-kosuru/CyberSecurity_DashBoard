package cybersecuritydashboard;

import com.formdev.flatlaf.FlatDarculaLaf;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CyberSecurityDashBoard extends JFrame {

    public int totalActiveHosts = 0;
    public int totalOpenPorts = 0;
    public int totalFilesEncrypted = 0;
    public int totalStrongPasswords = 0;

    private AnalysisPanel analysisPanel;

    public CyberSecurityDashBoard() {
        setTitle("Cybersecurity Dashboard v1.0");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();

        // main tools
        tabs.addTab("Network Scan", new NetworkScanPanel(this));
        tabs.addTab("Port Scan", new PortScanPanel(this));
        tabs.addTab("File Encrypt", new FileEncryptPanel(this));
        tabs.addTab("Password Check", new PasswordPanel(this));

        // analysis tab
        analysisPanel = new AnalysisPanel(this);
        tabs.addTab("Analysis", analysisPanel);

        // refresh analysis when that tab is selected
        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int index = tabs.getSelectedIndex();
                String title = tabs.getTitleAt(index);
                if ("Analysis".equals(title)) {
                    analysisPanel.refresh();
                }
            }
        });

        add(tabs);              // normal content (no BackgroundPanel)
        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            FlatDarculaLaf.setup();   // dark modern look and feel [web:248][web:308]
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new CyberSecurityDashBoard());
    }
}

package cybersecuritydashboard;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkScanPanel extends JPanel {

    private CyberSecurityDashBoard parent;
    private JTextField subnetField;
    private JButton scanButton;
    private JTextArea outputArea;

    public NetworkScanPanel(CyberSecurityDashBoard parent) {
        this.parent = parent;
        // overall panel style
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(15, 15, 20));

        // top bar
        JPanel top = new JPanel();
        top.setBackground(new Color(25, 25, 35));
        top.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));

        JLabel title = new JLabel("Network Scanner");
        title.setForeground(new Color(0, 200, 255));
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        top.add(title);

        top.add(new JLabel("Subnet:"));
        subnetField = new JTextField("192.168.3.", 12);
        subnetField.setBackground(new Color(35, 35, 45));
        subnetField.setForeground(Color.WHITE);
        top.add(subnetField);

        scanButton = new JButton("Scan Network (Fast)");
        top.add(scanButton);

        add(top, BorderLayout.NORTH);

        // output area in "terminal" style
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(10, 10, 15));
        outputArea.setForeground(new Color(0, 255, 128));
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        outputArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80)),
                "Scan Results",
                0, 0,
                new Font("Segoe UI", Font.PLAIN, 12),
                new Color(180, 180, 200)
        ));

        add(scroll, BorderLayout.CENTER);

        scanButton.addActionListener(e -> startScan());
    }

    private void startScan() {
        scanButton.setEnabled(false);
        outputArea.setText("[*] Starting network scan...\n");
        parent.totalActiveHosts = 0;

        String subnet = subnetField.getText().trim();
        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int i = 1; i < 255; i++) {
            final int host = i;
            executor.submit(() -> checkHost(subnet, host));
        }
        executor.shutdown();

        new Thread(() -> {
            try {
                while (!executor.isTerminated()) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException ignored) {}
            SwingUtilities.invokeLater(() -> {
                outputArea.append("\n[*] Scan complete. Active hosts: " +
                        parent.totalActiveHosts + "\n");
                scanButton.setEnabled(true);
            });
        }).start();
    }

    private void checkHost(String subnet, int host) {
        String ip = subnet + host;
        try {
            InetAddress addr = InetAddress.getByName(ip);
            if (addr.isReachable(300)) {
                parent.totalActiveHosts++;
                SwingUtilities.invokeLater(() ->
                        outputArea.append("[+] " + ip + "  ACTIVE\n"));
            }
        } catch (Exception ignored) {}
    }
}

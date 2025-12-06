package cybersecuritydashboard;

import javax.swing.*;
import java.awt.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PortScanPanel extends JPanel {

    private CyberSecurityDashBoard parent;

    private JTextField ipField, startPortField, endPortField;
    private JButton commonScanButton;
    private JButton fullScanButton;
    private JTextArea outputArea;

    // important common ports
    private final List<Integer> commonPorts = Arrays.asList(
            21, 22, 23, 25, 53, 80, 110, 135, 139, 143, 443, 445, 3389
    ); // [web:77][web:119]

    public PortScanPanel(CyberSecurityDashBoard parent) {
        this.parent = parent;

        // main style
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(15, 15, 20));

        // top bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(new Color(25, 25, 35));

        JLabel title = new JLabel("Port Scanner");
        title.setForeground(new Color(255, 180, 0));
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        top.add(title);

        top.add(new JLabel("Target IP:"));
        ipField = new JTextField("192.168.3.1", 12);
        ipField.setBackground(new Color(35, 35, 45));
        ipField.setForeground(Color.WHITE);
        top.add(ipField);

        top.add(new JLabel("Start:"));
        startPortField = new JTextField("1", 4);
        startPortField.setBackground(new Color(35, 35, 45));
        startPortField.setForeground(Color.WHITE);
        top.add(startPortField);

        top.add(new JLabel("End:"));
        endPortField = new JTextField("1024", 4);
        endPortField.setBackground(new Color(35, 35, 45));
        endPortField.setForeground(Color.WHITE);
        top.add(endPortField);

        commonScanButton = new JButton("1) Fast (common ports)");
        fullScanButton   = new JButton("2) Full (range, skip common)");
        fullScanButton.setEnabled(false); // unlocked after fast scan

        top.add(commonScanButton);
        top.add(fullScanButton);

        add(top, BorderLayout.NORTH);

        // output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(10, 10, 15));
        outputArea.setForeground(new Color(0, 255, 180));
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        outputArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80)),
                "Port Scan Results",
                0, 0,
                new Font("Segoe UI", Font.PLAIN, 12),
                new Color(180, 180, 200)
        ));

        add(scroll, BorderLayout.CENTER);

        // actions
        commonScanButton.addActionListener(e -> startCommonScan());
        fullScanButton.addActionListener(e -> startFullScan());
    }

    private void startCommonScan() {
        String ip = ipField.getText().trim();
        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter target IP");
            return;
        }

        commonScanButton.setEnabled(false);
        fullScanButton.setEnabled(false);
        outputArea.setText("[*] Fast scan on common ports...\n");
        parent.totalOpenPorts = 0; // reset [web:75]

        ExecutorService executor = Executors.newFixedThreadPool(20); // [web:88]
        for (int port : commonPorts) {
            executor.submit(() -> scanSinglePort(ip, port));
        }
        executor.shutdown();

        new Thread(() -> {
            waitForExecutor(executor);
            SwingUtilities.invokeLater(() -> {
                outputArea.append("\n[*] Common port scan complete. Open ports: "
                        + parent.totalOpenPorts + "\n");
                commonScanButton.setEnabled(true);
                fullScanButton.setEnabled(true); // now allow full scan
            });
        }).start();
    }

    private void startFullScan() {
        String ip = ipField.getText().trim();
        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter target IP");
            return;
        }

        int start, end;
        try {
            start = Integer.parseInt(startPortField.getText().trim());
            end   = Integer.parseInt(endPortField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ports must be numbers");
            return;
        }
        if (start < 1 || end > 65535 || start > end) {
            JOptionPane.showMessageDialog(this, "Port range must be 1–65535 and start ≤ end");
            return;
        }

        commonScanButton.setEnabled(false);
        fullScanButton.setEnabled(false);
        outputArea.append("\n[*] Full scan from " + start + " to " + end +
                " (excluding common ports)...\n");

        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (int port = start; port <= end; port++) {
            final int p = port;
            if (commonPorts.contains(p)) continue; // skip already scanned [web:75]
            executor.submit(() -> scanSinglePort(ip, p));
        }
        executor.shutdown();

        new Thread(() -> {
            waitForExecutor(executor);
            SwingUtilities.invokeLater(() -> {
                outputArea.append("\n[*] Full range scan complete. Total open ports: "
                        + parent.totalOpenPorts + "\n");
                commonScanButton.setEnabled(true);
                fullScanButton.setEnabled(true);
            });
        }).start();
    }

    private void waitForExecutor(ExecutorService executor) {
        try {
            while (!executor.isTerminated()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException ignored) {}
    }

    private void scanSinglePort(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), 100); // short timeout [web:76][web:85]
            parent.totalOpenPorts++;
            SwingUtilities.invokeLater(() ->
                    outputArea.append("[+] Port " + port + " OPEN\n"));
        } catch (Exception ignored) {
            // closed/filtered; ignore
        }
    }
}

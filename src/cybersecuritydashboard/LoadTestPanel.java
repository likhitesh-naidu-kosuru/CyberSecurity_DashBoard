package cybersecuritydashboard;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoadTestPanel extends JPanel {

    private CyberSecurityDashBoard parent;
    private JTextField ipField;
    private JTextField portField;
    private JSpinner threadsSpinner;
    private JSpinner requestsSpinner;
    private JButton portScanButton;
    private JButton httpLoadButton;
    private JTextArea logArea;
    private JLabel statusLabel;

    public LoadTestPanel(CyberSecurityDashBoard parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);

        // Top panel: inputs
        JPanel topPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        topPanel.setOpaque(true);
        topPanel.setBackground(new Color(40, 40, 40));
        topPanel.setBorder(new TitledBorder("Load Test Configuration"));

        // IP Address field
        JLabel ipLabel = new JLabel("Target IP:");
        ipLabel.setForeground(Color.CYAN);
        ipField = new JTextField("192.168.1.1", 20);
        ipField.setBackground(new Color(50, 50, 50));
        ipField.setForeground(Color.WHITE);
        topPanel.add(ipLabel);
        topPanel.add(ipField);

        // Port field
        JLabel portLabel = new JLabel("Target Port:");
        portLabel.setForeground(Color.CYAN);
        portField = new JTextField("80", 20);
        portField.setBackground(new Color(50, 50, 50));
        portField.setForeground(Color.WHITE);
        topPanel.add(portLabel);
        topPanel.add(portField);

        // Threads spinner
        JLabel threadsLabel = new JLabel("Concurrent Threads:");
        threadsLabel.setForeground(Color.CYAN);
        threadsSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 10));
        topPanel.add(threadsLabel);
        topPanel.add(threadsSpinner);

        // Requests spinner
        JLabel requestsLabel = new JLabel("Requests per Thread:");
        requestsLabel.setForeground(Color.CYAN);
        requestsSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 100));
        topPanel.add(requestsLabel);
        topPanel.add(requestsSpinner);

        // Port Scan Load Test button
        portScanButton = new JButton("Port Scan Load Test");
        portScanButton.setBackground(new Color(255, 87, 34));
        portScanButton.setForeground(Color.WHITE);
        portScanButton.setFont(new Font("Arial", Font.BOLD, 12));
        portScanButton.addActionListener(this::startPortScanLoadTest);
        topPanel.add(portScanButton);

        // HTTP Load Test button
        httpLoadButton = new JButton("HTTP Load Test");
        httpLoadButton.setBackground(new Color(0, 150, 136));
        httpLoadButton.setForeground(Color.WHITE);
        httpLoadButton.setFont(new Font("Arial", Font.BOLD, 12));
        httpLoadButton.addActionListener(this::startHttpLoadTest);
        topPanel.add(httpLoadButton);

        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.GREEN);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        JPanel statusPanel = new JPanel();
        statusPanel.setOpaque(false);
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Log area
        logArea = new JTextArea();
        logArea.setBackground(new Color(20, 20, 20));
        logArea.setForeground(Color.GREEN);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new TitledBorder("Test Log"));
        add(scrollPane, BorderLayout.CENTER);
    }

    // ==================== PORT SCAN LOAD TEST ====================
    private void startPortScanLoadTest(ActionEvent e) {
        String ip = ipField.getText().trim();
        String port = portField.getText().trim();
        int threads = (Integer) threadsSpinner.getValue();
        int requests = (Integer) requestsSpinner.getValue();

        if (ip.isEmpty() || port.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter IP and Port", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int portNum;
        try {
            portNum = Integer.parseInt(port);
            if (portNum < 1 || portNum > 65535) {
                JOptionPane.showMessageDialog(this, "Port must be 1-65535", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Port must be a number", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        logArea.setText("");
        log("=== Port Scan Load Test Started ===");
        log("Target IP: " + ip);
        log("Target Port: " + portNum);
        log("Threads: " + threads);
        log("Connection attempts per thread: " + requests);
        log("Total attempts: " + (threads * requests));
        log("Start time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        log("");

        portScanButton.setEnabled(false);
        httpLoadButton.setEnabled(false);
        statusLabel.setText("Port Scan Testing...");
        statusLabel.setForeground(Color.YELLOW);

        new Thread(() -> runPortScanLoadTest(ip, portNum, threads, requests)).start();
    }

    private void runPortScanLoadTest(String ip, int port, int threads, int requests) {
        long startTime = System.currentTimeMillis();
        final int[] openCount = {0};
        final int[] closedCount = {0};
        final long[] totalTime = {0};
        final int[] completedThreads = {0};

        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                for (int r = 0; r < requests; r++) {
                    try {
                        long reqStart = System.currentTimeMillis();
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(ip, port), 5000);
                        long reqTime = System.currentTimeMillis() - reqStart;

                        synchronized (openCount) {
                            openCount[0]++;
                            totalTime[0] += reqTime;
                        }
                        socket.close();
                    } catch (Exception ex) {
                        synchronized (openCount) {
                            closedCount[0]++;
                        }
                    }
                }

                synchronized (openCount) {
                    completedThreads[0]++;
                    if (completedThreads[0] == threads) {
                        long totalTime_ms = System.currentTimeMillis() - startTime;

                        log("");
                        log("=== Port Scan Test Complete ===");
                        log("Total time: " + totalTime_ms + " ms");
                        log("Port OPEN responses: " + openCount[0]);
                        log("Port CLOSED responses: " + closedCount[0]);
                        log("Open rate: " + (openCount[0] > 0 ? Math.round((double) openCount[0] / (openCount[0] + closedCount[0]) * 100) : 0) + "%");
                        log("Avg connection time: " + (openCount[0] > 0 ? (totalTime[0] / openCount[0]) : 0) + " ms");
                        log("Connections per second: " + Math.round((double) (openCount[0] + closedCount[0]) / (totalTime_ms / 1000.0) * 100.0) / 100.0);
                        log("End time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                        log("");
                        log("Port Status: " + (openCount[0] > 0 ? "OPEN" : "CLOSED"));

                        SwingUtilities.invokeLater(() -> {
                            portScanButton.setEnabled(true);
                            httpLoadButton.setEnabled(true);
                            statusLabel.setText("Complete");
                            statusLabel.setForeground(Color.GREEN);
                        });
                    }
                }
            }).start();
        }
    }

    // ==================== HTTP LOAD TEST ====================
    private void startHttpLoadTest(ActionEvent e) {
        String ip = ipField.getText().trim();
        String port = portField.getText().trim();
        int threads = (Integer) threadsSpinner.getValue();
        int requests = (Integer) requestsSpinner.getValue();

        if (ip.isEmpty() || port.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter IP and Port", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int portNum;
        try {
            portNum = Integer.parseInt(port);
            if (portNum < 1 || portNum > 65535) {
                JOptionPane.showMessageDialog(this, "Port must be 1-65535", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Port must be a number", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String targetUrl = "http://" + ip + ":" + portNum + "/";

        logArea.setText("");
        log("=== HTTP Load Test Started ===");
        log("Target IP: " + ip);
        log("Target Port: " + portNum);
        log("Full URL: " + targetUrl);
        log("Threads: " + threads);
        log("HTTP Requests per thread: " + requests);
        log("Total HTTP requests: " + (threads * requests));
        log("Start time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        log("");

        portScanButton.setEnabled(false);
        httpLoadButton.setEnabled(false);
        statusLabel.setText("HTTP Load Testing...");
        statusLabel.setForeground(Color.YELLOW);

        new Thread(() -> runHttpLoadTest(targetUrl, threads, requests)).start();
    }

    private void runHttpLoadTest(String targetUrl, int threads, int requests) {
        long startTime = System.currentTimeMillis();
        final int[] successCount = {0};
        final int[] failureCount = {0};
        final long[] totalTime = {0};
        final int[] completedThreads = {0};

        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                for (int r = 0; r < requests; r++) {
                    try {
                        long reqStart = System.currentTimeMillis();
                        HttpURLConnection conn = (HttpURLConnection) new URL(targetUrl).openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);
                        int responseCode = conn.getResponseCode();
                        long reqTime = System.currentTimeMillis() - reqStart;

                        synchronized (successCount) {
                            if (responseCode >= 200 && responseCode < 400) {
                                successCount[0]++;
                            } else {
                                failureCount[0]++;
                            }
                            totalTime[0] += reqTime;
                        }
                        conn.disconnect();
                    } catch (Exception ex) {
                        synchronized (successCount) {
                            failureCount[0]++;
                        }
                    }
                }

                synchronized (successCount) {
                    completedThreads[0]++;
                    if (completedThreads[0] == threads) {
                        long totalTime_ms = System.currentTimeMillis() - startTime;

                        log("");
                        log("=== HTTP Load Test Complete ===");
                        log("Total time: " + totalTime_ms + " ms");
                        log("Successful HTTP responses: " + successCount[0]);
                        log("Failed HTTP responses: " + failureCount[0]);
                        log("Success rate: " + (successCount[0] > 0 ? Math.round((double) successCount[0] / (successCount[0] + failureCount[0]) * 100) : 0) + "%");
                        log("Avg response time: " + (successCount[0] > 0 ? (totalTime[0] / successCount[0]) : 0) + " ms");
                        log("HTTP Requests per second: " + Math.round((double) (successCount[0] + failureCount[0]) / (totalTime_ms / 1000.0) * 100.0) / 100.0);
                        log("End time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                        log("");
                        log("Server Status: " + (successCount[0] > 0 ? "RESPONDING" : "NOT RESPONDING"));

                        SwingUtilities.invokeLater(() -> {
                            portScanButton.setEnabled(true);
                            httpLoadButton.setEnabled(true);
                            statusLabel.setText("Complete");
                            statusLabel.setForeground(Color.GREEN);
                        });
                    }
                }
            }).start();
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
}

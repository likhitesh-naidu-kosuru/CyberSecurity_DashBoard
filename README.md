#Cybersecurity Dashboard:
  A Java Swing desktop application that provides a small toolkit for basic cybersecurity analysis. It includes a network scanner, port scanner, file encryption       tool, password strength checker, and an analysis dashboard that summarizes results.

Features
.Network Scanner

  .Scans a given subnet (for example 192.168.1.*) and lists active hosts.

  .Uses multithreading and configurable timeouts to reduce scan time.

.Port Scanner

  .Fast scan of common ports (21, 22, 23, 25, 53, 80, 110, 135, 139, 143, 443, 445, 3389).

  .Full range scan with start/end ports, skipping ports already scanned in the fast pass.

  .Shows open ports per host and counts them for analysis.

.File Encryption / Decryption

  .Encrypts and decrypts files using AES in CBC mode.

  .Derives the key from a user password using PBKDF2 (password‑based key derivation).

  .Writes encrypted output as <filename>.enc and decrypted as <filename>.dec.

.Password Strength Checker

  .Evaluates password strength (WEAK / MEDIUM / STRONG).

  .Uses regular expressions to check for digits, lowercase, uppercase, special characters, length, and spaces.

  .Gives suggestions to improve weak passwords.

.Analysis Dashboard

  .Displays total active hosts found.

  .Displays total open ports detected.

  .Displays total files encrypted.

  .Displays number of strong passwords checked in this session.

.Tech Stack
  .Language: Java

  .GUI: Swing

  .IDE (used during development): Apache NetBeans

  .Look & Feel: FlatLaf dark theme (FlatDarcula)

  .Crypto APIs: Java Cryptography Extension (JCE) classes for AES and PBKDF2

.Project Structure
  .CyberSecurityDashBoard.java – Main frame and tabbed dashboard.

  .NetworkScanPanel.java – Subnet scanning UI and logic.

  .PortScanPanel.java – Fast + full port scanning UI and logic.

  .FileEncryptPanel.java – File selection, AES encrypt/decrypt, and log view.

  .PasswordPanel.java – Password strength checker UI and rules.

  .AnalysisPanel.java – Summary cards for all counters.

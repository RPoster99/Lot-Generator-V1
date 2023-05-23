import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import javax.imageio.ImageIO;
import java.io.*;

public class LotTrackerV1 {
    private static int currentNumber = 1;
    private static OutlineLabel codeLabel, buttonLabel;
    private static Color sapphireBlue = new Color(15, 82, 186);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LotTrackerV1::createAndShowGUI);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:AlphanumericCodes.db")) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");

                Statement stmt = conn.createStatement();
                stmt.execute("CREATE TABLE IF NOT EXISTS codes (id INTEGER PRIMARY KEY, code TEXT)");

                ResultSet rs = stmt.executeQuery("SELECT MAX(code) AS maxCode FROM codes");
                if (rs.next()) {
                    String maxCode = rs.getString("maxCode");
                    if (maxCode != null) {
                        currentNumber = Integer.parseInt(maxCode.substring(2)) + 1;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void createAndShowGUI() {
        // Set Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to cross-platform
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                // Not much we can do here
            }
        }

        // Create JFrame
        JFrame frame = new JFrame("Lot Tracker V1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 350);

        // Set Background Image
        try {
            frame.setContentPane(new JLabel(new ImageIcon(ImageIO.read(new File("/C:/Users/Poster/EmmiLogo.jpg")))));
        } catch (IOException e) {
            // Handle the exception if the image file cannot be loaded
            e.printStackTrace();
        }

        // Create Components
        codeLabel = new OutlineLabel();
        codeLabel.setText("ER" + String.format("%05d", currentNumber));
        codeLabel.setFont(new Font("Abadi", Font.BOLD, 45));
        codeLabel.setForeground(sapphireBlue);
        codeLabel.setOutlineColor(Color.GRAY);

        // Create a JButton
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(500, 100));
        button.setBackground(sapphireBlue);
        button.setLayout(new BorderLayout());  // set layout manager for JButton

        // Create a custom JLabel for the button text
        buttonLabel = new OutlineLabel();
        buttonLabel.setText("Accept Lot?");
        buttonLabel.setFont(new Font("Abadi", Font.BOLD, 40));
        buttonLabel.setForeground(Color.WHITE);
        buttonLabel.setOutlineColor(Color.GRAY);
        buttonLabel.setHorizontalAlignment(JLabel.CENTER);  // center horizontally
        buttonLabel.setVerticalAlignment(JLabel.CENTER);    // center vertically

        // Add the buttonLabel to the center of the button
        button.add(buttonLabel, BorderLayout.CENTER);

        // Button Action Listener
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirmed = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to use this Lot?", "Confirmation",
                        JOptionPane.YES_NO_OPTION);

                if (confirmed == JOptionPane.YES_OPTION) {
                    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:AlphanumericCodes.db")) {
                        if (conn != null) {
                            String code = codeLabel.getText();
                            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO codes(code) VALUES(?)");
                            pstmt.setString(1, code);
                            pstmt.executeUpdate();
                            currentNumber++;
                            codeLabel.setText("ER" + String.format("%05d", currentNumber));
                        }
                    } catch (SQLException ex) {
                        if (ex.getErrorCode() == 19) { // SQLite constraint violation
                            System.out.println("Lot already exists. Generating next Lot.");
                            currentNumber++;
                            codeLabel.setText("ER" + String.format("%05d", currentNumber));
                        } else {
                            System.out.println(ex.getMessage());
                        }
                    }
                }
            }
        });

        // Add Components to JFrame
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(codeLabel, BorderLayout.NORTH);
        frame.getContentPane().add(button, BorderLayout.SOUTH);

        // Display the window
        frame.pack();
        frame.setVisible(true);
    }

    static class OutlineLabel extends JLabel {
        private Color outlineColor = Color.black;

        public void setOutlineColor(Color c) {
            outlineColor = c;
            repaint();
        }

        public void paintComponent(Graphics g) {
            String text = getText();
            if (text == null || text.length() == 0) {
                super.paintComponent(g);
                return;
            }

            // Create a copy of the Graphics instance
            Graphics2D g2d = (Graphics2D) g.create();

            // Enable Anti-Aliasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Get the FontMetrics
            FontMetrics fm = g2d.getFontMetrics();

            // Determine the width of the text
            int textWidth = fm.stringWidth(text);
            // Determine the height of the text
            int textHeight = fm.getHeight();

            // Center the text horizontally and vertically
            int x = (getWidth() - textWidth) / 2;
            int y = ((getHeight() - textHeight) / 2) + fm.getAscent();

            // Draw the outline
            g2d.setColor(outlineColor);
            g2d.drawString(text, x-1, y-1);
            g2d.drawString(text, x-1, y+1);
            g2d.drawString(text, x+1, y-1);
            g2d.drawString(text, x+1, y+1);

            // Draw the text
            g2d.setColor(getForeground());
            g2d.drawString(text, x, y);

            // Dispose the Graphics2D instance
            g2d.dispose();
        }
    }
}

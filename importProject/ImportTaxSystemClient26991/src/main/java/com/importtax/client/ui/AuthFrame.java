package com.importtax.client.ui;

import com.importtax.client.util.UIConstants;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class AuthFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String CARD_LOGIN    = "LOGIN";
    private static final String CARD_REGISTER = "REGISTER";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     cardPanel  = new JPanel(cardLayout);

    private LoginPanel    loginPanel;
    private RegisterPanel registerPanel;
    private JLabel        dateTimeLabel;

    public AuthFrame() {
        setTitle(UIConstants.APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 720);
        setMinimumSize(new Dimension(860, 600));
        setLocationRelativeTo(null);
        setBackground(UIConstants.BACKGROUND_COLOR);

        loginPanel    = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);

        cardPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        cardPanel.add(loginPanel,    CARD_LOGIN);
        cardPanel.add(registerPanel, CARD_REGISTER);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.add(buildBrandPanel(), BorderLayout.WEST);
        root.add(cardPanel,         BorderLayout.CENTER);

        setContentPane(root);
        showLogin();
        startClock();
    }

    public void showLogin() {
        cardLayout.show(cardPanel, CARD_LOGIN);
    }

    public void showRegister() {
        cardLayout.show(cardPanel, CARD_REGISTER);
    }

    public void showLoginWithUsername(String username) {
        loginPanel.prefillUsername(username);
        showLogin();
    }

    // ── Persistent brand panel ─────────────────────────────────────────────
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.SIDEBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UIConstants.withAlpha(UIConstants.PRIMARY_COLOR, 35));
                g2.fillOval(-80, -80, 300, 300);
                g2.setColor(UIConstants.withAlpha(UIConstants.PRIMARY_COLOR, 20));
                g2.fillOval(getWidth() - 160, getHeight() - 160, 260, 260);
                g2.setColor(UIConstants.SIDEBAR_BORDER);
                g2.fillRect(getWidth() - 4, 0, 4, getHeight());
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setLayout(new MigLayout("fill, insets 48 40 48 40", "[center, grow]",
            "[grow][][][][][grow]"));

        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.withAlpha(Color.WHITE, 18));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UIConstants.withAlpha(Color.WHITE, 50));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(84, 84));
        badge.setLayout(new MigLayout("fill, insets 0", "[center]", "[center]"));
        JLabel logoLetter = new JLabel("IT");
        logoLetter.setFont(new Font("Segoe UI", Font.BOLD, 34));
        logoLetter.setForeground(Color.WHITE);
        badge.add(logoLetter);
        panel.add(badge, "cell 0 1, wrap, gapbottom 26");

        JLabel title = new JLabel("IMPORT TAX");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        panel.add(title, "cell 0 2, wrap");

        JLabel subtitle = new JLabel("MANAGEMENT SYSTEM");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        subtitle.setForeground(UIConstants.PRIMARY_LIGHT);
        panel.add(subtitle, "cell 0 3, wrap, gapbottom 28");

        JLabel desc = new JLabel("<html><center>Enterprise-grade distributed<br/>tax processing platform</center></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(new Color(180, 180, 180));
        panel.add(desc, "cell 0 4, wrap");

        dateTimeLabel = new JLabel(" ");
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dateTimeLabel.setForeground(UIConstants.TEXT_MUTED);
        panel.add(dateTimeLabel, "cell 0 5");

        return panel;
    }

    private void startClock() {
        Thread t = new Thread(() -> {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String text = LocalDateTime.now().format(fmt);
                    SwingUtilities.invokeLater(() -> dateTimeLabel.setText(text));
                    Thread.sleep(60_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "auth-clock");
        t.setDaemon(true);
        t.start();
    }
}

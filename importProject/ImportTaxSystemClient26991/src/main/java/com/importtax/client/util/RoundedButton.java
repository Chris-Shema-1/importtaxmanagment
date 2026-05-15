package com.importtax.client.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * RoundedButton - Custom JButton with rounded corners and modern styling.
 * Supports hover effects and customizable colors.
 *
 * @author Import Tax System Development Team
 * @version 1.0.0
 */
public class RoundedButton extends JButton {

    private static final long serialVersionUID = 1L;

    private final int arcRadius;
    private Color defaultColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;
    private Color borderColor;
    private int borderWidth;
    private boolean isHovered = false;
    private boolean isPressed = false;

    /**
     * Creates a RoundedButton with specified parameters.
     *
     * @param text The button text
     * @param arcRadius The radius for rounded corners
     * @param defaultColor The default background color
     * @param hoverColor The hover background color
     * @param textColor The text color
     */
    public RoundedButton(String text, int arcRadius, Color defaultColor,
            Color hoverColor, Color textColor) {
        super(text);
        this.arcRadius = arcRadius;
        this.defaultColor = defaultColor;
        this.hoverColor = hoverColor;
        this.pressedColor = defaultColor.darker();
        this.textColor = textColor;
        this.borderColor = defaultColor.darker();
        this.borderWidth = 0;

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(UIConstants.FONT_BUTTON);
        setForeground(textColor);
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    /**
     * Creates a RoundedButton with default styling.
     *
     * @param text The button text
     */
    public RoundedButton(String text) {
        this(text, UIConstants.BORDER_RADIUS, UIConstants.PRIMARY_COLOR,
                UIConstants.PRIMARY_LIGHT, Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

        // Determine background color based on state
        Color bgColor = defaultColor;
        if (isPressed) {
            bgColor = pressedColor;
        } else if (isHovered) {
            bgColor = hoverColor;
        }

        // Draw background with rounded corners
        g2d.setColor(bgColor);
        g2d.fillRoundRect(0, 0, width, height, arcRadius * 2, arcRadius * 2);

        // Draw border if enabled
        if (borderWidth > 0 && borderColor != null) {
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(borderWidth));
            g2d.drawRoundRect(borderWidth / 2, borderWidth / 2,
                    width - borderWidth, height - borderWidth,
                    arcRadius * 2, arcRadius * 2);
        }

        // Draw text
        super.paintComponent(g);
    }

    /**
     * Sets the border properties.
     *
     * @param borderColor The border color
     * @param borderWidth The border width
     */
    public void setBorderProperties(Color borderColor, int borderWidth) {
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
    }

    /**
     * Sets the colors for different states.
     *
     * @param defaultColor The default color
     * @param hoverColor The hover color
     * @param pressedColor The pressed color
     */
    public void setStateColors(Color defaultColor, Color hoverColor, Color pressedColor) {
        this.defaultColor = defaultColor;
        this.hoverColor = hoverColor;
        this.pressedColor = pressedColor;
        repaint();
    }
}

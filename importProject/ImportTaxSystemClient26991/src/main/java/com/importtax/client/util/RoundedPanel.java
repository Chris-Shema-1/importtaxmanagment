package com.importtax.client.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * RoundedPanel - Custom JPanel with rounded corners and optional border.
 * Provides a modern appearance for UI components.
 *
 * @author Import Tax System Development Team
 * @version 1.0.0
 */
public class RoundedPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final int arcWidth;
    private final int arcHeight;
    private final Color borderColor;
    private final int borderWidth;
    private final Color backgroundColor;

    /**
     * Creates a RoundedPanel with specified parameters.
     *
     * @param arcWidth The width of the arc for rounded corners
     * @param arcHeight The height of the arc for rounded corners
     * @param backgroundColor The background color of the panel
     * @param borderColor The border color (null for no border)
     * @param borderWidth The border width
     */
    public RoundedPanel(int arcWidth, int arcHeight, Color backgroundColor,
            Color borderColor, int borderWidth) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        setOpaque(false);
    }

    /**
     * Creates a RoundedPanel with default border width.
     *
     * @param arcWidth The width of the arc for rounded corners
     * @param arcHeight The height of the arc for rounded corners
     * @param backgroundColor The background color of the panel
     * @param borderColor The border color (null for no border)
     */
    public RoundedPanel(int arcWidth, int arcHeight, Color backgroundColor, Color borderColor) {
        this(arcWidth, arcHeight, backgroundColor, borderColor, 1);
    }

    /**
     * Creates a RoundedPanel without border.
     *
     * @param arcWidth The width of the arc for rounded corners
     * @param arcHeight The height of the arc for rounded corners
     * @param backgroundColor The background color of the panel
     */
    public RoundedPanel(int arcWidth, int arcHeight, Color backgroundColor) {
        this(arcWidth, arcHeight, backgroundColor, null, 0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

        // Draw background with rounded corners
        if (backgroundColor != null) {
            g2d.setColor(backgroundColor);
            g2d.fillRoundRect(0, 0, width, height, arcWidth, arcHeight);
        }

        // Draw border if specified
        if (borderColor != null && borderWidth > 0) {
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(borderWidth));
            g2d.drawRoundRect(borderWidth / 2, borderWidth / 2,
                    width - borderWidth, height - borderWidth,
                    arcWidth, arcHeight);
        }
    }
}

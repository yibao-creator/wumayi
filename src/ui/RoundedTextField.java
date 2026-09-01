package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 【外观】圆角半透明输入框，模拟玻璃质感
 */
public class RoundedTextField extends JTextField {

    private final int radius = 12;   // 圆角大小

    public RoundedTextField(int columns) {
        super(columns);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));   // 文字和边缘留白
    }

    public RoundedTextField(String text, int columns) {
        super(text, columns);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 半透明白色圆角底
        RoundRectangle2D rect = new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, radius, radius);
        g2.setColor(new Color(255, 255, 255, 210));
        g2.fill(rect);
        // 白色描边
        g2.setColor(new Color(255, 255, 255, 180));
        g2.draw(rect);

        g2.dispose();
        super.paintComponent(g);
    }
}

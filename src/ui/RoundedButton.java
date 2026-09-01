package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 【外观】圆角渐变按钮：鼠标悬停变亮、按下变深、禁用变灰
 */
public class RoundedButton extends JButton {

    private final int radius = 18;   // 圆角大小

    public RoundedButton(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setBorder(BorderFactory.createEmptyBorder(10, 26, 10, 26));
        setForeground(Colors.TEXT_LIGHT);
        setFont(Colors.FONT_BUTTON);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));   // 鼠标小手
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        if (!isEnabled()) {
            // 禁用状态：灰色
            g2.setColor(new Color(170, 178, 190));
            g2.fill(rect);
        } else if (getModel().isPressed()) {
            // 按下：颜色变深
            g2.setColor(Colors.PRIMARY_DARK.darker());
            g2.fill(rect);
        } else if (getModel().isRollover()) {
            // 鼠标悬停：颜色变亮
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(120, 185, 255),
                    0, getHeight(), new Color(64, 138, 240));
            g2.setPaint(gp);
            g2.fill(rect);
        } else {
            // 普通状态：蓝色渐变
            GradientPaint gp = new GradientPaint(
                    0, 0, Colors.PRIMARY_LIGHT,
                    0, getHeight(), Colors.PRIMARY_DARK);
            g2.setPaint(gp);
            g2.fill(rect);
        }

        // 顶部高光，玻璃反光感
        g2.setColor(new Color(255, 255, 255, 90));
        g2.fill(new RoundRectangle2D.Double(3, 3, getWidth() - 7, getHeight() / 2 - 3, radius, radius));

        g2.dispose();
        super.paintComponent(g);   // 画文字/图标
    }
}
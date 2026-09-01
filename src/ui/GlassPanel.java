package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 【外观】液态玻璃面板：半透明圆角 + 白色描边 + 阴影 + 斜向高光
 *
 * 透明度由构造参数 Color 的 alpha 决定（如 new Color(255,255,255,150) 就是半透明）。
 * 支持淡入动画：setGlassAlpha(0~目标值)。
 */
public class GlassPanel extends JPanel {

    private final int radius;          // 圆角大小（数字越大角越圆）
    private final Color fillColor;     // 面板底色
    private final int targetAlpha;     // 期望的最终透明度（来自构造参数）
    private int alpha;                 // 当前透明度（动画过程中变化）

    public GlassPanel(int radius, Color fillColor) {
        this.radius = radius;
        this.fillColor = fillColor;
        this.targetAlpha = fillColor.getAlpha();   // 记住构造参数里的透明度
        this.alpha = targetAlpha;
        setOpaque(false);
    }

    /** 设置当前透明度（淡入动画用）：最低 0 全透明，最高不超过目标透明度 */
    public void setGlassAlpha(int alpha) {
        this.alpha = Math.max(0, Math.min(targetAlpha, alpha));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. 底部阴影
        for (int i = 5; i >= 1; i--) {
            g2.setColor(Colors.SHADOW);
            g2.fill(new RoundRectangle2D.Double(2, 6 + i, getWidth() - 4, getHeight() - 2, radius, radius));
        }

        RoundRectangle2D glass = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        // 2. 玻璃本体：用当前透明度（动画结束后停在 targetAlpha）
        Color current = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), alpha);
        g2.setColor(current);
        g2.fill(glass);

        // 3. 斜向白色高光
        GradientPaint shine = new GradientPaint(
                0, 0, new Color(255, 255, 255, 110),
                getWidth(), getHeight(), new Color(255, 255, 255, 0));
        g2.setPaint(shine);
        g2.fill(glass);

        // 4. 白色高光描边
        g2.setColor(new Color(255, 255, 255, 190));
        g2.setStroke(new BasicStroke(1.4f));
        g2.draw(glass);

        g2.dispose();
        super.paintComponent(g);
    }
}
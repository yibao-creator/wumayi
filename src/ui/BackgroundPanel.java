package ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * 【外观】带背景图片的面板
 *
 * 用法：new BackgroundPanel("图片路径")，图片会自动"等比裁剪铺满"整个窗口。
 * 图片路径为 null 或不存在时，自动显示纯色背景（不会报错）。
 */
public class BackgroundPanel extends JPanel {

    private Image backgroundImage;   // 背景图片

    public BackgroundPanel(String imagePath) {
        // 图片加载失败时的底色（想换颜色改 RGB 三个数字）
        setBackground(new Color(210, 220, 235));

        // 路径存在才加载图片，避免路径写错导致程序崩溃
        if (imagePath != null) {
            File file = new File(imagePath);
            if (file.exists()) {
                backgroundImage = new ImageIcon(file.getAbsolutePath()).getImage();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            int panelW = getWidth();
            int panelH = getHeight();
            int imgW = backgroundImage.getWidth(this);
            int imgH = backgroundImage.getHeight(this);
            if (imgW <= 0 || imgH <= 0) {
                return;
            }

            // 等比缩放：取较大比例，保证铺满整个窗口（多余部分自动居中裁掉）
            double scale = Math.max((double) panelW / imgW, (double) panelH / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int x = (panelW - drawW) / 2;   // 水平居中
            int y = (panelH - drawH) / 2;   // 垂直居中

            g.drawImage(backgroundImage, x, y, drawW, drawH, this);
        }
    }
}
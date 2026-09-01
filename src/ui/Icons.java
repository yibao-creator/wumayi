package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 【外观】程序内绘制的小图标（纯代码画，不需要外部图片文件）
 *
 * 按钮上的小图标都来自这里，想改图标样式改对应 case 里的画图代码即可。
 */
public final class Icons {

    private static final int SIZE = 18;   // 图标像素大小

    public static ImageIcon upload() {
        return make("upload");
    }

    public static ImageIcon download() {
        return make("download");
    }

    public static ImageIcon refresh() {
        return make("refresh");
    }

    public static ImageIcon delete() {
        return make("delete");
    }

    public static ImageIcon rename() {
        return make("rename");
    }

    private static ImageIcon make(String type) {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (type) {
            case "upload" -> {
                g.drawLine(9, 3, 9, 12);                          // 箭杆
                g.fillPolygon(new int[]{4, 14, 9}, new int[]{5, 5, 1}, 3);  // 向上的箭头
                g.drawLine(3, 15, 15, 15);                        // 底部托盘
            }
            case "download" -> {
                g.drawLine(9, 2, 9, 12);                          // 箭杆
                g.fillPolygon(new int[]{4, 14, 9}, new int[]{11, 11, 16}, 3);  // 向下的箭头
                g.drawLine(3, 16, 15, 16);                        // 底部托盘
            }
            case "refresh" -> {
                g.drawArc(3, 3, 12, 12, 0, 300);                  // 圆形箭头弧
                g.fillPolygon(new int[]{13, 17, 14}, new int[]{1, 4, 7}, 3);  // 箭头尖
            }
            case "delete" -> {
                g.drawRect(5, 7, 8, 8);                           // 垃圾桶身
                g.drawLine(3, 6, 15, 6);                          // 盖子
                g.drawLine(8, 6, 8, 4);                           // 把手
                g.drawLine(10, 6, 10, 4);
            }
            case "rename" -> {
                g.drawLine(4, 14, 5, 10);                         // 笔杆底部
                g.drawLine(5, 10, 12, 3);                         // 笔身
                g.drawLine(12, 3, 15, 6);                         // 笔尖上沿
                g.drawLine(13, 4, 10, 7);                         // 笔尖下沿
            }
        }
        g.dispose();
        return new ImageIcon(img);
    }

    private Icons() {
    }
}

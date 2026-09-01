package ui;

import java.awt.*;

/**
 * 【外观】统一颜色体系
 *
 * 所有界面的颜色、字体都在这里集中定义：
 * 想改主题色，只需要改这里，整个项目一起变。
 */
public final class Colors {

    // ---------- 主色系（蓝色） ----------
    public static final Color PRIMARY = new Color(66, 133, 244);       // 主色
    public static final Color PRIMARY_LIGHT = new Color(96, 165, 250); // 主色-亮
    public static final Color PRIMARY_DARK = new Color(43, 105, 220);  // 主色-暗

    // ---------- 文字 ----------
    public static final Color TEXT_MAIN = new Color(60, 66, 78);       // 主要文字
    public static final Color TEXT_LIGHT = new Color(255, 255, 255);   // 浅色文字（按钮上）
    public static final Color STATUS_TEXT = new Color(100, 108, 120);  // 次要/状态文字

    // ---------- 面板 / 表格 ----------
    public static final Color PANEL_BG = new Color(240, 242, 246);     // 普通面板背景
    public static final Color TABLE_HEADER_BG = new Color(226, 233, 243); // 表头背景
    public static final Color TABLE_STRIPE = new Color(245, 248, 253); // 表格斑马纹
    public static final Color TABLE_SELECTION = new Color(190, 218, 255); // 选中行
    public static final Color SHADOW = new Color(0, 0, 0, 14);         // 阴影

    // ---------- 字体 ----------
    public static final Font FONT_NORMAL = new Font("微软雅黑", Font.PLAIN, 14);
    public static final Font FONT_TITLE = new Font("微软雅黑", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("微软雅黑", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("微软雅黑", Font.BOLD, 15);

    private Colors() {
    }
}

import ui.LoginFrame;

/**
 * 程序入口：启动登录窗口
 */
public class Main {
    public static void main(String[] args) {
        // 【外观】使用 Windows 原生界面风格（不加这行的话是 Java 默认的灰色风格）
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // 设置失败就保持默认外观，不影响运行
        }
        javax.swing.SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
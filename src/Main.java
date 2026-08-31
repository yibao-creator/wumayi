import ui.LoginFrame;

/**
 * 程序入口：启动登录窗口
 */
public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
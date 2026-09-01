package ui;

import business.ControllerFactory;
import business.IFtpController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * 登录窗口（成员4负责）—— 液态玻璃（玻璃拟态）风格
 *
 * 界面元素：标题、服务器IP、端口、账号、密码输入框 + 登录按钮。
 * 不写任何 Socket / 文件逻辑，只调用成员3的 IFtpController。
 */
public class LoginFrame extends JFrame {

    /** 【外观】背景图片路径：把想用的照片路径填在这里（支持绝对路径，如 D:/photo.jpg） */
    private static final String BG_IMAGE_PATH = "src/resources/background.png";

    /** 【外观】窗口图标路径：想换图标就覆盖 src/resources/icon.png */
    private static final String APP_ICON_PATH = "src/resources/icon.png";

    // 输入框用圆角玻璃样式
    private final JTextField ipField = new RoundedTextField("127.0.0.1", 14);
    private final JTextField portField = new RoundedTextField("21", 6);
    private final JTextField userField = new RoundedTextField(12);
    private final JPasswordField pwdField = new RoundedPasswordField(12);
    private final JButton eyeBtn = new JButton("显示");   // 密码显示/隐藏按钮
    private final JButton loginBtn = new RoundedButton("登 录");

    private boolean passwordVisible = false;   // 密码当前是否明文显示

    /** 只依赖接口；具体是假控制器还是成员3的实现，由 ControllerFactory 决定 */
    private final IFtpController controller = ControllerFactory.create();

    public LoginFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 【外观】用图片做窗口背景
        BackgroundPanel bg = new BackgroundPanel(findResource(BG_IMAGE_PATH));
        setContentPane(bg);

        // 【外观】液态玻璃主面板\玻璃的透明度就是 GlassPanel()里最后一个数字，越小越透明、越大越实
        GlassPanel glass = new GlassPanel(24, new Color(255, 255, 255, 150));
        glass.setPreferredSize(new Dimension(460, 400));
        glass.setLayout(new BorderLayout(0, 18));
        glass.setBorder(BorderFactory.createEmptyBorder(28, 44, 26, 44));

        // 顶部标题区：大标题 + 副标题
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("FTP 客户端");
        title.setFont(Colors.FONT_TITLE);
        title.setForeground(Colors.TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("计算机网络课程设计 · 文件传输");
        subtitle.setFont(Colors.FONT_SUBTITLE);
        subtitle.setForeground(Colors.STATUS_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        // 表单网格：4 行（标签 + 输入框）
        JPanel grid = new JPanel(new GridLayout(4, 2, 10, 16));
        grid.setOpaque(false);
        grid.add(makeLabel("服务器 IP"));
        grid.add(ipField);
        grid.add(makeLabel("端口"));
        grid.add(portField);
        grid.add(makeLabel("账号"));
        grid.add(userField);
        grid.add(makeLabel("密码"));
        JPanel pwdRow = new JPanel(new BorderLayout(6, 0));   // 密码框 + 显示按钮一行
        pwdRow.setOpaque(false);
        pwdRow.add(pwdField, BorderLayout.CENTER);
        pwdRow.add(eyeBtn, BorderLayout.EAST);
        grid.add(pwdRow);

        // 登录按钮单独一行
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(loginBtn);

        glass.add(titlePanel, BorderLayout.NORTH);
        glass.add(grid, BorderLayout.CENTER);
        glass.add(btnPanel, BorderLayout.SOUTH);

        // 玻璃面板居中
        bg.setLayout(new GridBagLayout());
        bg.add(glass);

        applyAppearance();

        loginBtn.addActionListener(e -> doLogin());
        eyeBtn.addActionListener(e -> togglePassword());
        getRootPane().setDefaultButton(loginBtn);   // 回车触发登录
        
        // 窗口打开时玻璃淡入
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                startFadeIn(glass);
            }
        });
    }

    // ==================== ★ 外观设置区 ★ ====================
    private void applyAppearance() {
        setTitle("FTP客户端 - 登录");
        setSize(640, 500);
        setLocationRelativeTo(null);

        // 输入框字体
        Font inputFont = new Font("微软雅黑", Font.PLAIN, 16);
        ipField.setFont(inputFont);
        portField.setFont(inputFont);
        userField.setFont(inputFont);
        pwdField.setFont(inputFont);

        // 密码"显示/隐藏"按钮样式
        eyeBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        eyeBtn.setForeground(Colors.PRIMARY);
        eyeBtn.setFocusPainted(false);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setBorderPainted(false);
        eyeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 背景照片加载失败时的底色
        getContentPane().setBackground(new Color(210, 220, 235));

        // 窗口图标
        String iconPath = findResource(APP_ICON_PATH);
        if (iconPath != null) {
            Image icon = new ImageIcon(iconPath).getImage();
            setIconImage(icon);
            if (Taskbar.isTaskbarSupported()) {
                try {
                    Taskbar.getTaskbar().setIconImage(icon);
                } catch (Exception ignored) {
                }
            }
        }
    }
    // ==================== 外观设置区结束 ====================

    /** 标签文字（统一字体颜色，想调字号改 16） */
    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        label.setForeground(Colors.TEXT_MAIN);
        return label;
    }

    /** 查找资源文件：优先按相对路径找，找不到再按工作目录找，实在找不到打印提示 */
    private static String findResource(String relativePath) {
        File f = new File(relativePath);
        if (f.exists()) {
            return f.getAbsolutePath();
        }
        f = new File(System.getProperty("user.dir"), relativePath);
        if (f.exists()) {
            return f.getAbsolutePath();
        }
        System.out.println("【提示】找不到资源: " + relativePath
                + " | 当前工作目录: " + System.getProperty("user.dir"));
        return null;
    }
    /** 密码明文/密文切换 */
    private void togglePassword() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            pwdField.setEchoChar((char) 0);   // 显示明文
            eyeBtn.setText("隐藏");
        } else {
            pwdField.setEchoChar('\u2022');   // 显示圆点
            eyeBtn.setText("显示");
        }
    }

    /** 玻璃面板淡入动画 */
    private void startFadeIn(GlassPanel glass) {
        glass.setGlassAlpha(0);
        int[] a = {0};
        new javax.swing.Timer(12, e -> {
            a[0] += 20;
            if (a[0] >= 255) {
                glass.setGlassAlpha(255);
                ((javax.swing.Timer) e.getSource()).stop();
            } else {
                glass.setGlassAlpha(a[0]);
            }
        }).start();
    }

    private void doLogin() {
        String ip = ipField.getText().trim();
        String portText = portField.getText().trim();
        String user = userField.getText().trim();
        String pwd = new String(pwdField.getPassword());

        if (ip.isEmpty() || portText.isEmpty() || user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "IP、端口、账号不能为空");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "端口必须是数字");
            return;
        }

        loginBtn.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return controller.login(ip, port, user, pwd);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(LoginFrame.this, "登录成功");
                        new MainFrame(controller, user, ip + ":" + port).setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "登录失败：账号或密码错误");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "连接失败：" + ex.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    loginBtn.setEnabled(true);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
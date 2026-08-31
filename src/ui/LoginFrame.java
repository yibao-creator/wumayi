package ui;

import business.ControllerFactory;
import business.IFtpController;

import javax.swing.*;
import java.awt.*;

/**
 * 登录窗口（成员4负责）
 *
 * 界面元素：服务器IP、端口、账号、密码输入框 + 登录按钮。
 * 不写任何 Socket / 文件逻辑，只调用成员3的 IFtpController。
 */
public class LoginFrame extends JFrame {

    private final JTextField ipField = new JTextField("127.0.0.1", 12);
    private final JTextField portField = new JTextField("21", 6);
    private final JTextField userField = new JTextField(10);
    private final JPasswordField pwdField = new JPasswordField(10);
    private final JButton loginBtn = new JButton("登录");

    /** 只依赖接口；具体是假控制器还是成员3的实现，由 ControllerFactory 决定 */
    private final IFtpController controller = ControllerFactory.create();

    public LoginFrame() {
        setTitle("FTP客户端 - 登录");
        setSize(380, 240);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        form.add(new JLabel("服务器 IP"));
        form.add(ipField);
        form.add(new JLabel("端口"));
        form.add(portField);
        form.add(new JLabel("账号"));
        form.add(userField);
        form.add(new JLabel("密码"));
        form.add(pwdField);

        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        bottom.add(loginBtn);

        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(loginBtn);   // 回车也能触发登录
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

        loginBtn.setEnabled(false);   // 防止重复点击
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // 网络操作放在后台线程，界面不会卡死（答辩加分点）
                return controller.login(ip, port, user, pwd);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(LoginFrame.this, "登录成功");
                        new MainFrame(controller).setVisible(true);
                        dispose();   // 关闭登录窗口
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
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
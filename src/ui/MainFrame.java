package ui;

import business.IFtpController;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

/**
 * 主功能窗口（成员4负责）—— 液态玻璃风格
 *
 * 功能：文件列表显示 + 刷新 / 上传 / 下载 / 删除 / 重命名（按钮 + 右键菜单 + 双击下载）。
 * 所有业务操作都调用成员3的 IFtpController，不写网络、不写文件读写逻辑。
 */
public class MainFrame extends JFrame {

    private static final String BG_IMAGE_PATH = "src/resources/background.png";
    private static final String APP_ICON_PATH = "src/resources/icon.png";

    private final IFtpController controller;
    private final String username;        // 当前登录用户（状态栏显示用）
    private final String serverAddress;   // 服务器地址（状态栏显示用）

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new String[]{"文件名", "大小"}, 0);
    private final JTable fileTable = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel("就绪");
    private final JProgressBar progressBar = new JProgressBar();

    public MainFrame(IFtpController controller, String username, String serverAddress) {
        this.controller = controller;
        this.username = username;
        this.serverAddress = serverAddress;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 背景照片 + 玻璃卡片
        BackgroundPanel bg = new BackgroundPanel(findResource(BG_IMAGE_PATH));
        setContentPane(bg);

        GlassPanel glass = new GlassPanel(20, new Color(255, 255, 255, 215));
        glass.setPreferredSize(new Dimension(820, 560));
        glass.setLayout(new BorderLayout(0, 10));
        glass.setBorder(BorderFactory.createEmptyBorder(16, 16, 10, 16));

        // 顶部工具栏
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        toolbar.setOpaque(false);
        toolbar.add(makeButton("刷新", Icons.refresh()));
        toolbar.add(makeButton("上传", Icons.upload()));
        toolbar.add(makeButton("下载", Icons.download()));
        toolbar.add(makeButton("删除", Icons.delete()));
        toolbar.add(makeButton("重命名", Icons.rename()));

        // 文件列表表格：斑马纹 + 排序 + 右键菜单 + 双击下载
        setupTable();
        JScrollPane scroll = new JScrollPane(fileTable);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(Colors.TABLE_HEADER_BG));

        // 底部状态栏：状态文字 + 忙碌进度条
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        statusBar.setOpaque(false);
        statusLabel.setFont(Colors.FONT_NORMAL);
        statusLabel.setForeground(Colors.STATUS_TEXT);
        statusLabel.setText("用户: " + username + " | 服务器: " + serverAddress);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(140, 16));
        statusBar.add(statusLabel);
        statusBar.add(progressBar);

        glass.add(toolbar, BorderLayout.NORTH);
        glass.add(scroll, BorderLayout.CENTER);
        glass.add(statusBar, BorderLayout.SOUTH);

        bg.setLayout(new GridBagLayout());
        bg.add(glass);

        applyAppearance();
        refreshList();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                startFadeIn(glass);
            }
        });
    }

    /** 表格美化：斑马纹、选中高亮、点击表头排序、右键菜单、双击下载 */
    private void setupTable() {
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setShowGrid(false);                       // 去掉网格线
        fileTable.setRowHeight(30);
        fileTable.setFont(Colors.FONT_NORMAL);
        fileTable.setRowSorter(new TableRowSorter<>(tableModel));   // 点击表头排序
        fileTable.setDefaultRenderer(Object.class, new GlassTableCellRenderer());

        fileTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        fileTable.getTableHeader().setBackground(Colors.TABLE_HEADER_BG);
        fileTable.getTableHeader().setForeground(Colors.TEXT_MAIN);

        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    doDownload();   // 双击文件直接下载
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopupIfNeeded(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopupIfNeeded(e);
            }

            private void showPopupIfNeeded(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                int row = fileTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    fileTable.setRowSelectionInterval(row, row);
                }
                JPopupMenu menu = new JPopupMenu();
                JMenuItem downloadItem = new JMenuItem("下载");
                JMenuItem deleteItem = new JMenuItem("删除");
                JMenuItem renameItem = new JMenuItem("重命名");
                downloadItem.addActionListener(ev -> doDownload());
                deleteItem.addActionListener(ev -> doDelete());
                renameItem.addActionListener(ev -> doRename());
                menu.add(downloadItem);
                menu.add(deleteItem);
                menu.add(renameItem);
                menu.show(fileTable, e.getX(), e.getY());
            }
        });
    }

    // ==================== ★ 外观设置区 ★ ====================
    private void applyAppearance() {
        setTitle("FTP客户端 - 文件管理");
        setSize(860, 600);
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(210, 220, 235));

        String iconPath = findResource(APP_ICON_PATH);
        if (iconPath != null) {
            setIconImage(new ImageIcon(iconPath).getImage());
        }
    }
    // ==================== 外观设置区结束 ====================

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
    /** 创建带图标和文字的玻璃按钮 */
    private JButton makeButton(String text, Icon icon) {
        RoundedButton button = new RoundedButton(text);
        if (icon != null) {
            button.setIcon(icon);
            button.setHorizontalTextPosition(SwingConstants.RIGHT);
            button.setIconTextGap(7);
        }
        return button;
    }

    /** 表格单元格渲染器：斑马纹 + 选中高亮 */
    private class GlassTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (isSelected) {
                c.setBackground(Colors.TABLE_SELECTION);
                c.setForeground(Colors.TEXT_MAIN);
            } else {
                c.setBackground(row % 2 == 0 ? Color.WHITE : Colors.TABLE_STRIPE);
                c.setForeground(Colors.TEXT_MAIN);
            }
            return c;
        }
    }

    /** 刷新文件列表 */
    private void refreshList() {
        setBusy(true, "正在刷新文件列表…");
        new SwingWorker<List<String[]>, Void>() {
            @Override
            protected List<String[]> doInBackground() throws Exception {
                return controller.listFiles();
            }

            @Override
            protected void done() {
                try {
                    List<String[]> rows = get();
                    tableModel.setRowCount(0);
                    for (String[] row : rows) {
                        tableModel.addRow(row);
                    }
                    if (rows.isEmpty()) {
                        statusLabel.setText("暂无文件");
                    } else {
                        statusLabel.setText("共 " + rows.size() + " 个文件");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("刷新失败");
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "刷新失败：" + ex.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setBusy(false, null);
                }
            }
        }.execute();
    }

    private void doUpload() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        runTask("上传", () -> controller.upload(file), this::refreshList);
    }

    private void doDownload() {
        String selected = getSelectedFileName();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "请先选择要下载的文件");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(selected));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String savePath = chooser.getSelectedFile().getAbsolutePath();
        runTask("下载", () -> controller.download(selected, savePath), null);
    }

    private void doDelete() {
        String selected = getSelectedFileName();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的文件");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "确定删除 " + selected + " ?", "确认删除",
                JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            runTask("删除", () -> controller.delete(selected), this::refreshList);
        }
    }

    private void doRename() {
        String selected = getSelectedFileName();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "请先选择要重命名的文件");
            return;
        }
        String newName = JOptionPane.showInputDialog(this, "新文件名：", selected);
        if (newName == null || newName.trim().isEmpty()) {
            return;
        }
        runTask("重命名", () -> controller.rename(selected, newName.trim()), this::refreshList);
    }

    /** 获取当前选中文件名（注意表格排序后要把显示行转回数据行） */
    private String getSelectedFileName() {
        int viewRow = fileTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = fileTable.convertRowIndexToModel(viewRow);
        return (String) tableModel.getValueAt(modelRow, 0);
    }

    /** 忙碌状态：显示/隐藏进度条 */
    private void setBusy(boolean busy, String message) {
        progressBar.setVisible(busy);
        if (message != null) {
            statusLabel.setText(message);
        }
    }

    /** 通用后台任务模板：状态栏提示 + 进度条 + 弹窗结果 */
    private void runTask(String actionName, Task task, Runnable onSuccess) {
        setBusy(true, "正在" + actionName + "…");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText(actionName + "成功");
                    JOptionPane.showMessageDialog(MainFrame.this, actionName + "成功");
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } catch (Exception ex) {
                    statusLabel.setText(actionName + "失败");
                    JOptionPane.showMessageDialog(MainFrame.this,
                            actionName + "失败：" + ex.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setBusy(false, null);
                }
            }
        }.execute();
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

    @FunctionalInterface
    private interface Task {
        void run() throws Exception;
    }
}
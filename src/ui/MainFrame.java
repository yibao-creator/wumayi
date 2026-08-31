package ui;

import business.IFtpController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * 主功能窗口（成员4负责）
 *
 * 功能：文件列表显示 + 刷新 / 上传 / 下载 / 删除 / 重命名按钮。
 * 所有业务操作都调用成员3的 IFtpController，不写网络、不写文件读写逻辑。
 */
public class MainFrame extends JFrame {

    private final IFtpController controller;
    private final DefaultTableModel tableModel =
            new DefaultTableModel(new String[]{"文件名", "大小"}, 0);
    private final JTable fileTable = new JTable(tableModel);

    public MainFrame(IFtpController controller) {
        this.controller = controller;
        setTitle("FTP客户端 - 文件管理");
        setSize(720, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setRowHeight(24);
        add(new JScrollPane(fileTable), BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        JButton refreshBtn = new JButton("刷新");
        JButton uploadBtn = new JButton("上传");
        JButton downloadBtn = new JButton("下载");
        JButton deleteBtn = new JButton("删除");
        JButton renameBtn = new JButton("重命名");
        toolbar.add(refreshBtn);
        toolbar.add(uploadBtn);
        toolbar.add(downloadBtn);
        toolbar.add(deleteBtn);
        toolbar.add(renameBtn);
        add(toolbar, BorderLayout.NORTH);

        refreshBtn.addActionListener(e -> refreshList());
        uploadBtn.addActionListener(e -> doUpload());
        downloadBtn.addActionListener(e -> doDownload());
        deleteBtn.addActionListener(e -> doDelete());
        renameBtn.addActionListener(e -> doRename());

        refreshList();   // 打开窗口自动刷新一次
    }

    /** 刷新文件列表：SwingWorker 防止界面卡死 */
    private void refreshList() {
        new SwingWorker<List<String[]>, Void>() {
            @Override
            protected List<String[]> doInBackground() throws Exception {
                return controller.listFiles();
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (String[] row : get()) {
                        tableModel.addRow(row);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "刷新失败：" + ex.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void doUpload() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();   // 只负责拿到用户选的文件，读写由业务层做
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

    private String getSelectedFileName() {
        int row = fileTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (String) tableModel.getValueAt(row, 0);
    }

    /**
     * 通用的后台任务模板：把"网络操作"放到后台线程，完成后在界面线程弹提示。
     * 这样刷新/上传/下载/删除/重命名的代码不会重复一大段。
     */
    private void runTask(String actionName, Task task, Runnable onSuccess) {
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
                    JOptionPane.showMessageDialog(MainFrame.this, actionName + "成功");
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            actionName + "失败：" + ex.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    @FunctionalInterface
    private interface Task {
        void run() throws Exception;
    }
}

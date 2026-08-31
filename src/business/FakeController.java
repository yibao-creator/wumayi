package business;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 假控制器：成员4本地开发 / 演示用，不依赖网络、不依赖其他成员代码。
 * 数据是假的，但方法签名和 IFtpController 完全一致，UI 可以完整跑通。
 */
public class FakeController implements IFtpController {

    private final List<String[]> files = new ArrayList<>();

    public FakeController() {
        files.add(new String[]{"课程设计报告.docx", "256 KB"});
        files.add(new String[]{"演示截图.png", "1.2 MB"});
        files.add(new String[]{"readme.txt", "2 KB"});
    }

    @Override
    public boolean login(String ip, int port, String username, String password) throws Exception {
        Thread.sleep(800);            // 模拟网络延时，方便看到"登录中"的按钮禁用效果
        return password != null && !password.isEmpty();   // 空密码模拟登录失败
    }

    @Override
    public List<String[]> listFiles() throws Exception {
        Thread.sleep(500);
        return files;
    }

    @Override
    public void upload(File localFile) throws Exception {
        Thread.sleep(800);
        files.add(new String[]{localFile.getName(), formatSize(localFile.length())});
    }

    @Override
    public void download(String remoteFileName, String localSavePath) throws Exception {
        Thread.sleep(800);            // 假装下载完成
    }

    @Override
    public boolean delete(String remoteFileName) throws Exception {
        Thread.sleep(500);
        files.removeIf(row -> row[0].equals(remoteFileName));
        return true;
    }

    @Override
    public boolean rename(String oldName, String newName) throws Exception {
        Thread.sleep(500);
        for (String[] row : files) {
            if (row[0].equals(oldName)) {
                row[0] = newName;
                return true;
            }
        }
        return false;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}

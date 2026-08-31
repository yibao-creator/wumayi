package ftpserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ClientHandler implements Runnable {
    private static final int BUFFER_SIZE = 8192;

    private final Socket socket;
    private final Path root;
    private boolean loggedIn;

    public ClientHandler(Socket socket, Path root) {
        this.socket = socket;
        this.root = root;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            while (true) {
                String command = in.readUTF();
                System.out.println("收到命令: " + command);

                switch (command) {
                    case "LOGIN":
                        handleLogin(in, out);
                        break;
                    case "LIST":
                        handleList(out);
                        break;
                    case "UPLOAD":
                        handleUpload(in, out);
                        break;
                    case "DOWNLOAD":
                        handleDownload(in, out);
                        break;
                    case "DELETE":
                        handleDelete(in, out);
                        break;
                    case "RENAME":
                        handleRename(in, out);
                        break;
                    case "QUIT":
                        out.writeUTF("BYE");
                        return;
                    default:
                        out.writeUTF("ERROR 不认识的命令: " + command);
                        break;
                }
            }
        } catch (EOFException e) {
            System.out.println("客户端断开连接");
        } catch (Exception e) {
            System.out.println("连接处理结束: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
                // 客户端已经断开时不需要再处理
            }
        }
    }

    private void handleLogin(DataInputStream in, DataOutputStream out) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();

        if ("admin".equals(username) && "123456".equals(password)) {
            loggedIn = true;
            out.writeUTF("OK 登录成功");
        } else {
            out.writeUTF("ERROR 账号或密码错误");
        }
    }

    private void handleList(DataOutputStream out) throws IOException {
        if (!requireLogin(out)) {
            return;
        }

        StringBuilder result = new StringBuilder();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    result.append(path.getFileName()).append(" [目录]").append(System.lineSeparator());
                } else {
                    result.append(path.getFileName())
                          .append(" [")
                          .append(Files.size(path))
                          .append(" 字节]")
                          .append(System.lineSeparator());
                }
            }
        }

        if (result.length() == 0) {
            result.append("(空目录)");
        }

        out.writeUTF("OK\n" + result.toString());
    }

    private void handleUpload(DataInputStream in, DataOutputStream out) throws IOException {
        if (!requireLogin(out)) {
            return;
        }

        String fileName = in.readUTF();
        long size = in.readLong();
        Path target = safeResolve(fileName, out);
        if (target == null) {
            return;
        }

        try (OutputStream fileOut = Files.newOutputStream(target)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            long remaining = size;

            while (remaining > 0) {
                int readSize = (int) Math.min(buffer.length, remaining);
                int count = in.read(buffer, 0, readSize);
                if (count < 0) {
                    throw new EOFException("文件上传中断");
                }
                fileOut.write(buffer, 0, count);
                remaining -= count;
            }
        }

        out.writeUTF("OK 上传成功");
    }

    private void handleDownload(DataInputStream in, DataOutputStream out) throws IOException {
        if (!requireLogin(out)) {
            return;
        }

        String fileName = in.readUTF();
        Path target = safeResolve(fileName, out);
        if (target == null) {
            return;
        }

        if (!Files.isRegularFile(target)) {
            out.writeUTF("ERROR 文件不存在");
            return;
        }

        out.writeUTF("OK " + Files.size(target));

        try (InputStream fileIn = Files.newInputStream(target)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        }

        out.flush();
    }

    private void handleDelete(DataInputStream in, DataOutputStream out) throws IOException {
        if (!requireLogin(out)) {
            return;
        }

        String fileName = in.readUTF();
        Path target = safeResolve(fileName, out);
        if (target == null) {
            return;
        }

        if (!Files.exists(target)) {
            out.writeUTF("ERROR 文件不存在");
            return;
        }

        Files.delete(target);
        out.writeUTF("OK 删除成功");
    }

    private void handleRename(DataInputStream in, DataOutputStream out) throws IOException {
        if (!requireLogin(out)) {
            return;
        }

        String oldName = in.readUTF();
        String newName = in.readUTF();
        Path oldPath = safeResolve(oldName, out);
        if (oldPath == null) {
            return;
        }
        Path newPath = safeResolve(newName, out);
        if (newPath == null) {
            return;
        }

        if (!Files.exists(oldPath)) {
            out.writeUTF("ERROR 文件不存在");
            return;
        }

        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        out.writeUTF("OK 重命名成功");
    }

    private boolean requireLogin(DataOutputStream out) throws IOException {
        if (!loggedIn) {
            out.writeUTF("ERROR 请先登录");
            return false;
        }
        return true;
    }

    private Path safeResolve(String fileName, DataOutputStream out) throws IOException {
        Path rootPath = root.toAbsolutePath().normalize();
        Path resolved = rootPath.resolve(fileName).normalize();

        if (!resolved.startsWith(rootPath)) {
            out.writeUTF("ERROR 非法文件名: " + fileName);
            return null;
        }

        return resolved;
    }
}

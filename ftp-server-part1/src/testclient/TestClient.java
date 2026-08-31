package testclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestClient {
    private static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("127.0.0.1", 2121);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            login(out, in);
            list(out, in);
            upload(out, in);
            list(out, in);
            download(out, in);
            rename(out, in);
            delete(out, in);

            out.writeUTF("QUIT");
            System.out.println("退出: " + in.readUTF());
        }
    }

    private static void login(DataOutputStream out, DataInputStream in) throws IOException {
        out.writeUTF("LOGIN");
        out.writeUTF("admin");
        out.writeUTF("123456");
        printResponse("登录", in.readUTF());
    }

    private static void list(DataOutputStream out, DataInputStream in) throws IOException {
        out.writeUTF("LIST");
        printResponse("列表", in.readUTF());
    }

    private static void upload(DataOutputStream out, DataInputStream in) throws IOException {
        String fileName = "hello.txt";
        byte[] data = "第一次上传测试内容".getBytes(StandardCharsets.UTF_8);

        out.writeUTF("UPLOAD");
        out.writeUTF(fileName);
        out.writeLong(data.length);
        out.write(data);
        out.flush();

        printResponse("上传", in.readUTF());
    }

    private static void download(DataOutputStream out, DataInputStream in) throws IOException {
        out.writeUTF("DOWNLOAD");
        out.writeUTF("hello.txt");

        String response = in.readUTF();
        if (!response.startsWith("OK")) {
            System.out.println("下载失败: " + response);
            return;
        }

        long size = Long.parseLong(response.substring(3).trim());
        Path target = Paths.get("downloaded_hello.txt");

        try (OutputStream fileOut = Files.newOutputStream(target)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            long remaining = size;

            while (remaining > 0) {
                int readSize = (int) Math.min(buffer.length, remaining);
                int count = in.read(buffer, 0, readSize);
                if (count < 0) {
                    throw new EOFException("下载中断");
                }
                fileOut.write(buffer, 0, count);
                remaining -= count;
            }
        }

        System.out.println("下载成功: " + target.toAbsolutePath());
    }

    private static void rename(DataOutputStream out, DataInputStream in) throws IOException {
        out.writeUTF("RENAME");
        out.writeUTF("hello.txt");
        out.writeUTF("renamed.txt");
        printResponse("重命名", in.readUTF());
    }

    private static void delete(DataOutputStream out, DataInputStream in) throws IOException {
        out.writeUTF("DELETE");
        out.writeUTF("renamed.txt");
        printResponse("删除", in.readUTF());
    }

    private static void printResponse(String action, String response) {
        System.out.println(action + " 结果: " + response.replace("\n", "\n  "));
    }
}

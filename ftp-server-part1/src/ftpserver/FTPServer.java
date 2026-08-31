package ftpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FTPServer {
    private final int port;
    private final Path root;

    public FTPServer(int port, Path root) {
        this.port = port;
        this.root = root;
    }

    public void start() throws IOException {
        Files.createDirectories(root);
        System.out.println("FTP服务器已启动，端口: " + port);
        System.out.println("文件仓库目录: " + root.toAbsolutePath());

        ExecutorService pool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(port);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("客户端已连接: " + socket.getRemoteSocketAddress());
                pool.submit(new ClientHandler(socket, root));
            }
        } finally {
            serverSocket.close();
        }
    }

    public static void main(String[] args) throws IOException {
        new FTPServer(2121, Paths.get("server_files")).start();
    }
}

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Temporary in-memory test server, ONLY for Member 2's black-window self-test.
 * During team integration, Member 1's real FTPServer.java takes over.
 */
public class FtpTestServer implements Closeable {

    private final ServerSocket serverSocket;
    private final Map<String, byte[]> files = new ConcurrentHashMap<String, byte[]>();
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private volatile boolean running = true;

    public FtpTestServer() throws IOException {
        this(0);
    }

    public FtpTestServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        files.put("hello.txt",
                "Hello from the test server.\r\nSecond line.\r\n".getBytes(StandardCharsets.UTF_8));
        files.put("readme.txt",
                "This is a README for Member 2 network test.".getBytes(StandardCharsets.UTF_8));
        files.put("sample.pdf",
                new byte[] {37, 80, 68, 70, 45, 49, 46, 52, 10, 37, 37, 69, 79, 70, 10});
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void start() {
        Thread acceptor = new Thread(new Runnable() {
            @Override
            public void run() {
                acceptLoop();
            }
        }, "ftp-test-acceptor");
        acceptor.setDaemon(true);
        acceptor.start();
    }

    private void acceptLoop() {
        while (running) {
            try {
                final Socket client = serverSocket.accept();
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        handleClient(client);
                    }
                });
            } catch (IOException e) {
                if (running) {
                    System.err.println("Accept failed: " + e.getMessage());
                }
            }
        }
    }

    private void handleClient(Socket client) {
        try (Socket socket = client) {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            sendLine(out, "220 FTP test server ready");
            String pendingRename = null;
            String line;
            while ((line = readLine(in)) != null) {
                String upper = line.toUpperCase(Locale.ROOT);
                System.out.println("[server] " + line);
                if (upper.startsWith("USER ")) {
                    sendLine(out, "331 Password required");
                } else if (upper.startsWith("PASS ")) {
                    sendLine(out, "230 Login OK");
                } else if (upper.equals("LIST")) {
                    sendLine(out, "150 Here comes the listing");
                    for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                        sendLine(out, "FILE|" + entry.getKey() + "|" + entry.getValue().length);
                    }
                    sendLine(out, "226 Directory send OK");
                } else if (upper.startsWith("RETR ")) {
                    String name = line.substring(5).trim();
                    byte[] data = files.get(name);
                    if (data == null) {
                        sendLine(out, "550 File not found: " + name);
                    } else {
                        sendLine(out, "150 " + data.length);
                        out.write(data);
                        out.flush();
                        sendLine(out, "226 Transfer complete");
                    }
                } else if (upper.startsWith("STOR ")) {
                    String rest = line.substring(5).trim();
                    int lastSpace = rest.lastIndexOf(' ');
                    if (lastSpace < 0) {
                        sendLine(out, "501 Bad STOR syntax");
                    } else {
                        String name = rest.substring(0, lastSpace);
                        long length;
                        try {
                            length = Long.parseLong(rest.substring(lastSpace + 1));
                        } catch (NumberFormatException e) {
                            sendLine(out, "501 Bad STOR length");
                            continue;
                        }
                        sendLine(out, "150 OK, send the data");
                        byte[] data = readBytes(in, length);
                        files.put(name, data);
                        sendLine(out, "226 Transfer complete");
                    }
                } else if (upper.startsWith("DELE ")) {
                    String name = line.substring(5).trim();
                    if (files.remove(name) != null) {
                        sendLine(out, "200 Delete OK");
                    } else {
                        sendLine(out, "550 File not found: " + name);
                    }
                } else if (upper.startsWith("RNFR ")) {
                    String oldName = line.substring(5).trim();
                    if (files.containsKey(oldName)) {
                        pendingRename = oldName;
                        sendLine(out, "350 Ready for RNTO");
                    } else {
                        pendingRename = null;
                        sendLine(out, "550 File not found: " + oldName);
                    }
                } else if (upper.startsWith("RNTO ")) {
                    String newName = line.substring(5).trim();
                    if (pendingRename == null) {
                        sendLine(out, "503 RNFR required first");
                    } else if (files.containsKey(newName)) {
                        sendLine(out, "550 Target already exists: " + newName);
                    } else {
                        byte[] data = files.remove(pendingRename);
                        files.put(newName, data);
                        sendLine(out, "250 Rename OK");
                    }
                    pendingRename = null;
                } else if (upper.equals("QUIT")) {
                    sendLine(out, "221 Bye");
                    break;
                } else {
                    sendLine(out, "500 Unknown command: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Client handling error: " + e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        try {
            serverSocket.close();
        } finally {
            pool.shutdownNow();
        }
    }

    private static byte[] readBytes(InputStream in, long length) throws IOException {
        if (length < 0 || length > Integer.MAX_VALUE) {
            throw new IOException("length too large: " + length);
        }
        byte[] data = new byte[(int) length];
        int offset = 0;
        while (offset < data.length) {
            int n = in.read(data, offset, data.length - offset);
            if (n < 0) {
                throw new IOException("client stopped sending data");
            }
            offset += n;
        }
        return data;
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') {
                break;
            }
            if (b != '\r') {
                buffer.write(b);
            }
        }
        if (b == -1 && buffer.size() == 0) {
            return null;
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void sendLine(OutputStream out, String line) throws IOException {
        out.write((line + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 2121;
        FtpTestServer server = new FtpTestServer(port);
        server.start();
        System.out.println("FTP test server listening on port " + server.getPort());
        while (true) {
            Thread.sleep(60000);
        }
    }
}

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-side network layer for the simplified FTP course project (Member 2).
 *
 * Responsibilities:
 * - connect / disconnect
 * - login with username and password
 * - send command lines and read server reply lines
 * - transfer file bytes for list / download / upload / delete / rename
 *
 * This class does NOT touch Swing UI and does NOT read/write local files.
 */
public class FtpNetworkClient {

    /** Network or protocol error thrown by this layer. */
    public static class FtpException extends IOException {
        public FtpException(String message) {
            super(message);
        }
    }

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int SO_TIMEOUT_MS = 15000;

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private boolean loggedIn;

    public FtpNetworkClient() {
    }

    public void connect(String host, int port) throws IOException {
        closeQuietly();
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
        socket.setSoTimeout(SO_TIMEOUT_MS);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        String greeting = readLine();
        if (greeting == null || !greeting.startsWith("220")) {
            throw new FtpException("server greeting error: " + greeting);
        }
        loggedIn = false;
    }

    public void login(String username, String password) throws IOException {
        requireConnected();
        sendLine("USER " + username);
        String reply = readReply();
        if (reply.startsWith("230")) {
            loggedIn = true;
            return;
        }
        if (!reply.startsWith("331")) {
            throw new FtpException("login failed: " + reply);
        }
        sendLine("PASS " + password);
        reply = readReply();
        if (!reply.startsWith("230")) {
            throw new FtpException("wrong username or password: " + reply);
        }
        loggedIn = true;
    }

    public List<String> listFiles() throws IOException {
        requireLogin();
        sendLine("LIST");
        String reply = readReply();
        if (!reply.startsWith("150")) {
            throw new FtpException("list failed: " + reply);
        }
        List<String> entries = new ArrayList<String>();
        while (true) {
            String line = readLine();
            if (line == null) {
                throw new FtpException("connection closed while reading list");
            }
            if (line.startsWith("226")) {
                break;
            }
            entries.add(line);
        }
        return entries;
    }

    public byte[] download(String remoteName) throws IOException {
        requireLogin();
        sendLine("RETR " + remoteName);
        String reply = readReply();
        if (!reply.startsWith("150")) {
            throw new FtpException("download failed: " + reply);
        }
        long length = parseLength(reply);
        if (length < 0 || length > Integer.MAX_VALUE) {
            throw new FtpException("invalid file length from server: " + reply);
        }
        byte[] data = new byte[(int) length];
        readFully(data);
        reply = readReply();
        if (!reply.startsWith("226")) {
            throw new FtpException("download verify failed: " + reply);
        }
        return data;
    }

    public void upload(String remoteName, byte[] data) throws IOException {
        upload(remoteName, new ByteArrayInputStream(data), data.length);
    }

    public void upload(String remoteName, InputStream data, long length) throws IOException {
        requireLogin();
        if (length < 0) {
            throw new FtpException("invalid upload length: " + length);
        }
        sendLine("STOR " + remoteName + " " + length);
        String reply = readReply();
        if (!reply.startsWith("150")) {
            throw new FtpException("upload failed: " + reply);
        }
        byte[] buffer = new byte[8192];
        long remaining = length;
        while (remaining > 0) {
            int n = data.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (n < 0) {
                throw new FtpException("local data stream ended early");
            }
            out.write(buffer, 0, n);
            remaining -= n;
        }
        out.flush();
        reply = readReply();
        if (!reply.startsWith("226")) {
            throw new FtpException("upload verify failed: " + reply);
        }
    }

    public String delete(String remoteName) throws IOException {
        requireLogin();
        sendLine("DELE " + remoteName);
        return readReply();
    }

    public String rename(String oldName, String newName) throws IOException {
        requireLogin();
        sendLine("RNFR " + oldName);
        String reply = readReply();
        if (!reply.startsWith("350")) {
            return reply;
        }
        sendLine("RNTO " + newName);
        return readReply();
    }

    public void disconnect() {
        if (socket != null) {
            try {
                sendLine("QUIT");
                readReply();
            } catch (IOException ignored) {
                // Connection may already be broken; just close it.
            } finally {
                closeQuietly();
            }
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private void requireConnected() throws IOException {
        if (!isConnected()) {
            throw new FtpException("not connected to server");
        }
    }

    private void requireLogin() throws IOException {
        requireConnected();
        if (!loggedIn) {
            throw new FtpException("please login first");
        }
    }

    private void sendLine(String line) throws IOException {
        out.write((line + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private String readReply() throws IOException {
        String line = readLine();
        if (line == null) {
            throw new FtpException("server closed the connection");
        }
        return line;
    }

    /**
     * Reads one text line byte by byte. We do NOT use BufferedReader here:
     * the same socket stream also carries binary file data, so a buffered
     * reader would swallow part of the file bytes.
     */
    private String readLine() throws IOException {
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

    private void readFully(byte[] data) throws IOException {
        int offset = 0;
        while (offset < data.length) {
            int n = in.read(data, offset, data.length - offset);
            if (n < 0) {
                throw new FtpException("file data transfer interrupted");
            }
            offset += n;
        }
    }

    private long parseLength(String reply) {
        int space = reply.indexOf(' ');
        if (space < 0) {
            return -1;
        }
        try {
            return Long.parseLong(reply.substring(space + 1).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void closeQuietly() {
        loggedIn = false;
        if (out != null) {
            try {
                out.close();
            } catch (IOException ignored) {
                // Ignore.
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException ignored) {
                // Ignore.
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // Ignore.
            }
        }
        out = null;
        in = null;
        socket = null;
    }
}

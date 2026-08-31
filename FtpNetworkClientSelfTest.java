import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Console self-test for Member 2's FtpNetworkClient.
 * It starts the temporary FtpTestServer, then verifies every network operation.
 */
public class FtpNetworkClientSelfTest {

    public static void main(String[] args) throws Exception {
        FtpTestServer server = new FtpTestServer();
        server.start();
        FtpNetworkClient client = new FtpNetworkClient();
        try {
            client.connect("127.0.0.1", server.getPort());
            System.out.println("[OK] connect");

            client.login("member2", "123456");
            System.out.println("[OK] login");

            List<String> list = client.listFiles();
            System.out.println("[OK] list, " + list.size() + " entries: " + list);

            byte[] downloaded = client.download("hello.txt");
            System.out.println("[OK] download " + downloaded.length + " bytes");

            String uploadData = "uploaded by Member 2";
            client.upload("upload.txt", uploadData.getBytes(StandardCharsets.UTF_8));
            System.out.println("[OK] upload");

            byte[] roundTrip = client.download("upload.txt");
            if (!Arrays.equals(uploadData.getBytes(StandardCharsets.UTF_8), roundTrip)) {
                throw new AssertionError("upload/download data mismatch");
            }
            System.out.println("[OK] upload/download round trip");

            String renameReply = client.rename("hello.txt", "hello2.txt");
            System.out.println("[OK] rename reply: " + renameReply);

            String deleteReply = client.delete("upload.txt");
            System.out.println("[OK] delete reply: " + deleteReply);

            client.disconnect();
            System.out.println("[OK] disconnect");

            System.out.println("ALL MEMBER 2 SELF TESTS PASSED");
        } finally {
            client.disconnect();
            server.close();
        }
    }
}

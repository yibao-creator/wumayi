package business;

import java.io.File;
import java.util.List;

/**
 * FTP 业务控制层接口 —— 这是全队的"对接协议"。
 *
 * 成员4（UI 层）只依赖本接口，不依赖成员3的具体实现；
 * 成员3实现本接口时，方法签名不要改。
 * 成员5的自定义异常（LoginException / NetworkException / FileTransferException）
 * 还没写好前，先统一用 Exception 兜底，界面 catch (Exception e) 即可，不影响联调。
 */
public interface IFtpController {

    /** 登录：成功返回 true；账号密码错误返回 false；网络异常抛异常 */
    boolean login(String ip, int port, String username, String password) throws Exception;

    /** 获取服务器文件列表，每项 String[]{文件名, 大小}，如 {"报告.docx", "256 KB"} */
    List<String[]> listFiles() throws Exception;

    /** 上传本地文件（读取文件由业务层负责，UI 只负责把 File 传进来） */
    void upload(File localFile) throws Exception;

    /** 下载服务器文件到本地路径（写文件由业务层负责） */
    void download(String remoteFileName, String localSavePath) throws Exception;

    /** 删除服务器文件 */
    boolean delete(String remoteFileName) throws Exception;

    /** 重命名服务器文件 */
    boolean rename(String oldName, String newName) throws Exception;
}

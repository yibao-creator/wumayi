package org.example.network;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * FTP客户端网络通信类
 *
 * 临时测试版本
 * 后续由成员2实现真正的Socket网络通信
 */
public class FtpNetworkClient {


    /**
     * 登录
     */
    public boolean login(String username, String password) {

        System.out.println("正在发送登录请求...");
        System.out.println("用户名：" + username);

        // 临时模拟登录成功
        return true;
    }


    /**
     * 获取服务器文件列表
     */
    public List<String> list() {

        System.out.println("正在获取服务器文件列表...");

        List<String> fileList = new ArrayList<>();

        // 临时模拟服务器文件
        fileList.add("test.txt");
        fileList.add("hello.txt");
        fileList.add("FTP文件夹");

        return fileList;
    }


    /**
     * 上传文件
     */
    public boolean upload(File file) {

        if (file == null || !file.exists()) {
            return false;
        }

        System.out.println("正在上传文件：" + file.getName());

        // 临时模拟上传成功
        return true;
    }


    /**
     * 下载文件
     */
    public boolean download(String fileName, File saveFile) {

        System.out.println("正在下载文件：" + fileName);
        System.out.println("保存位置：" + saveFile.getAbsolutePath());

        // 临时模拟下载成功
        return true;
    }


    /**
     * 删除文件
     */
    public boolean delete(String fileName) {

        System.out.println("正在删除服务器文件：" + fileName);

        // 临时模拟删除成功
        return true;
    }


    /**
     * 重命名文件
     */
    public boolean rename(String oldName, String newName) {

        System.out.println(
                "正在重命名：" +
                        oldName +
                        " -> " +
                        newName
        );

        // 临时模拟重命名成功
        return true;
    }
}
package org.example.controller;

import org.example.network.FtpNetworkClient;
import org.example.util.LocalFileManager;

import java.io.File;
import java.util.List;

/**
 * FTP业务控制器
 *
 * 负责：
 * 1. 接收UI层的业务请求
 * 2. 调用网络层与FTP服务器通信
 * 3. 调用本地文件管理类处理文件
 * 4. 统一处理业务过程中的异常
 */
public class FtpController {

    // 网络通信对象
    private final FtpNetworkClient networkClient;

    // 本地文件管理对象
    private final LocalFileManager fileManager;


    /**
     * 构造方法
     *
     * @param networkClient 网络通信对象
     */
    public FtpController(FtpNetworkClient networkClient) {
        this.networkClient = networkClient;
        this.fileManager = new LocalFileManager();
    }


    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录是否成功
     */
    public boolean login(String username, String password) {

        // 判断用户名是否为空
        if (username == null || username.trim().isEmpty()) {
            System.out.println("用户名不能为空");
            return false;
        }

        // 判断密码是否为空
        if (password == null || password.trim().isEmpty()) {
            System.out.println("密码不能为空");
            return false;
        }

        try {
            return networkClient.login(username, password);
        } catch (Exception e) {
            System.out.println("登录失败：" + e.getMessage());
            return false;
        }
    }


    /**
     * 获取服务器文件列表
     *
     * @return 文件列表
     */
    public List<String> getFileList() {

        try {
            return networkClient.list();
        } catch (Exception e) {
            System.out.println("获取文件列表失败：" + e.getMessage());
            return null;
        }
    }


    /**
     * 上传本地文件到服务器
     *
     * @param localPath 本地文件路径
     * @return 上传是否成功
     */
    public boolean uploadFile(String localPath) {

        // 判断路径是否为空
        if (localPath == null || localPath.trim().isEmpty()) {
            System.out.println("文件路径不能为空");
            return false;
        }

        File file = new File(localPath);

        // 判断文件是否存在
        if (!fileManager.exists(file)) {
            System.out.println("上传失败：文件不存在");
            return false;
        }

        // 判断是否为空文件
        if (file.length() == 0) {
            System.out.println("上传失败：不允许上传空文件");
            return false;
        }

        try {
            return networkClient.upload(file);
        } catch (Exception e) {
            System.out.println("上传失败：" + e.getMessage());
            return false;
        }
    }


    /**
     * 从服务器下载文件
     *
     * @param fileName 服务器上的文件名
     * @param savePath 本地保存路径
     * @return 下载是否成功
     */
    public boolean downloadFile(String fileName, String savePath) {

        // 判断文件名
        if (fileName == null || fileName.trim().isEmpty()) {
            System.out.println("下载失败：文件名不能为空");
            return false;
        }

        // 判断保存路径
        if (savePath == null || savePath.trim().isEmpty()) {
            System.out.println("下载失败：保存路径不能为空");
            return false;
        }

        File saveFile = new File(savePath);

        try {
            boolean result = networkClient.download(fileName, saveFile);

            if (result) {
                System.out.println("下载成功：" + saveFile.getAbsolutePath());
            }

            return result;

        } catch (Exception e) {
            System.out.println("下载失败：" + e.getMessage());
            return false;
        }
    }


    /**
     * 删除服务器上的文件或文件夹
     *
     * @param fileName 文件名
     * @return 删除是否成功
     */
    public boolean deleteFile(String fileName) {

        if (fileName == null || fileName.trim().isEmpty()) {
            System.out.println("删除失败：文件名不能为空");
            return false;
        }

        try {
            return networkClient.delete(fileName);
        } catch (Exception e) {
            System.out.println("删除失败：" + e.getMessage());
            return false;
        }
    }


    /**
     * 重命名服务器上的文件或文件夹
     *
     * @param oldName 原文件名
     * @param newName 新文件名
     * @return 重命名是否成功
     */
    public boolean renameFile(String oldName, String newName) {

        if (oldName == null || oldName.trim().isEmpty()) {
            System.out.println("重命名失败：原文件名不能为空");
            return false;
        }

        if (newName == null || newName.trim().isEmpty()) {
            System.out.println("重命名失败：新文件名不能为空");
            return false;
        }

        try {
            return networkClient.rename(oldName, newName);
        } catch (Exception e) {
            System.out.println("重命名失败：" + e.getMessage());
            return false;
        }
    }
}
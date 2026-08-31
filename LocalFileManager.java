package org.example.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 本地文件管理类
 *
 * 负责客户端本地文件的读取、保存和检查
 */
public class LocalFileManager {


    /**
     * 判断文件是否存在，并且是普通文件
     *
     * @param file 文件对象
     * @return 文件是否存在
     */
    public boolean exists(File file) {

        if (file == null) {
            return false;
        }

        return file.exists() && file.isFile();
    }


    /**
     * 获取文件大小
     *
     * @param file 文件对象
     * @return 文件大小，单位：字节
     */
    public long getFileSize(File file) {

        if (!exists(file)) {
            return 0;
        }

        return file.length();
    }


    /**
     * 读取本地文件
     *
     * @param file 文件对象
     * @return 文件输入流
     * @throws IOException 文件读取失败时抛出异常
     */
    public InputStream readFile(File file) throws IOException {

        if (!exists(file)) {
            throw new IOException("文件不存在：" + file);
        }

        return new FileInputStream(file);
    }


    /**
     * 保存下载的文件
     *
     * @param input 输入流
     * @param file  保存位置
     * @return 是否保存成功
     */
    public boolean saveFile(InputStream input, File file) {

        if (input == null || file == null) {
            return false;
        }

        try {
            // 获取父目录
            File parent = file.getParentFile();

            // 如果父目录不存在，则创建
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            // 使用try-with-resources自动关闭流
            try (InputStream in = input;
                 FileOutputStream out = new FileOutputStream(file)) {

                byte[] buffer = new byte[4096];
                int length;

                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }

                out.flush();
            }

            return true;

        } catch (IOException e) {
            System.out.println("文件保存失败：" + e.getMessage());
            return false;
        }
    }
}
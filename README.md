# 成员 2：客户端网络通信层

## 你的任务范围

你只负责网络收发，不写 Swing 界面，也不读写本地文件：

- 连接服务器、断开服务器
- 账号密码登录
- 刷新列表、下载、上传、删除、重命名
- 接收服务器的文字消息和文件数据流
- 处理连不上服务器、连接中断等网络错误

## 我为你准备好的文件

- `FtpNetworkClient.java`：你的正式产出，成员 3 以后只调用这个类的公开方法。
- `FtpTestServer.java`：**只用于你黑窗口自测的临时假服务器**，不是成员 1 的正式服务端，正式联调时以成员 1 的 `FTPServer.java` 为准。
- `FtpNetworkClientSelfTest.java`：自动跑一遍连接、登录、列表、上传、下载、重命名、删除、断开的自测程序。
- `run_self_test.cmd`：双击即可编译并运行自测。

## 运行自测

双击 `run_self_test.cmd`，或者在命令行里执行：

```text
javac -encoding UTF-8 FtpNetworkClient.java FtpTestServer.java FtpNetworkClientSelfTest.java
java FtpNetworkClientSelfTest
```

看到 `ALL MEMBER 2 SELF TESTS PASSED` 就说明网络层全部指令收发正常。

## 约定协议（请发给成员 1 照着实现）

这是一个简化版 FTP 协议：一条 TCP 连接同时传文字指令和文件数据，文本统一 UTF-8，行尾用 `\r\n`。

| 客户端发送 | 服务器回复 | 说明 |
| --- | --- | --- |
| 连接后什么都不发 | `220 ...` | 欢迎消息，客户端靠它判断连接成功 |
| `USER 用户名` | `331 Password required` | 请求密码 |
| `PASS 密码` | `230 Login OK` 或 `530 ...` | 登录结果 |
| `LIST` | `150 ...`，然后每行一条 `FILE|文件名|大小`，最后 `226 ...` | 文件列表 |
| `RETR 文件名` | `150 文件长度`，然后发文件原始字节，最后 `226 ...` | 下载 |
| `STOR 文件名 长度` | `150 ...`，然后客户端发原始字节，最后 `226 ...` | 上传 |
| `DELE 文件名` | `200 ...` 或 `550 ...` | 删除 |
| `RNFR 旧名` | `350 Ready for RNTO` 或 `550 ...` | 重命名第一步 |
| `RNTO 新名` | `250 Rename OK` 或 `550 ...` | 重命名第二步 |
| `QUIT` | `221 Bye` | 断开 |

注意：文件名里不能有换行；`LIST` 的每行格式建议固定为 `FILE|文件名|大小`，成员 3 和成员 4 解析起来最简单。

## 给成员 3 的调用接口

```java
connect(String host, int port)
login(String username, String password)
listFiles()                    // 返回 List<String>
download(String remoteName)    // 返回 byte[]
upload(String remoteName, byte[] data)
upload(String remoteName, InputStream data, long length)
delete(String remoteName)      // 返回服务器回复文字
rename(String oldName, String newName) // 返回服务器回复文字
disconnect()
```

网络层不写本地文件：成员 3 负责把本地文件读成字节再上传，把下载得到的字节保存成本地文件。

## 答辩时你可以讲

1. TCP 通信流程：Socket 连接、发送指令、读取回复、关闭连接。
2. 为什么网络层不直接用 `BufferedReader` 读取文件数据，避免缓冲吞掉二进制字节。
3. 黑窗口自测：启动假服务器，逐个验证登录、列表、上传、下载、删除、重命名。

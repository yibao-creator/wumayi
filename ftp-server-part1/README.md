# 第一部分：FTP 服务端开发教程

你负责的是 **FTP 服务器**，可以把它理解成整个项目的“后台仓库管理员”。

客户端会通过网线（网络连接）来找你，然后发命令：

- 登录
- 查看文件列表
- 上传文件
- 下载文件
- 重命名文件
- 删除文件

你要做的就是：收到命令，操作服务器本地真实的文件，然后把成功或失败的结果告诉客户端。

---

## 学习顺序

不要一上来就想写完所有功能。按下面顺序走：

1. 先和全队定好“对话协议”
2. 写出一个能启动的服务端
3. 一个客户端连接进来后，单独开一条处理线
4. 先做登录
5. 再做文件列表
6. 再做上传和下载
7. 最后做删除和重命名
8. 自己用测试程序完整跑一遍

---

## 第 0 步：先和全队定协议

服务端和客户端就像两个人打电话，必须先说好：

- 电话号码是多少：这里用端口 `2121`
- 每句话怎么说：每条命令是一个字符串
- 文件怎么传：先传文件名和大小，再传文件内容
- 成功失败怎么表示：返回 `OK ...` 或 `ERROR ...`

本示例的协议如下：

| 操作 | 客户端发送 | 服务器返回 |
| --- | --- | --- |
| 登录 | `LOGIN` + 用户名 + 密码 | `OK 登录成功` |
| 列表 | `LIST` | `OK` + 文件列表文字 |
| 上传 | `UPLOAD` + 文件名 + 文件大小 + 文件内容 | `OK 上传成功` |
| 下载 | `DOWNLOAD` + 文件名 | `OK 文件大小` + 文件内容 |
| 删除 | `DELETE` + 文件名 | `OK 删除成功` |
| 重命名 | `RENAME` + 旧文件名 + 新文件名 | `OK 重命名成功` |
| 退出 | `QUIT` | `BYE` |

这个协议必须让写“客户端网络层”的同学看到。只要全队按同一套规则写，你的服务端就能被整个项目调用。

---

## 第 1 步：看懂项目结构

示例代码放在下面这些文件里：

```text
ftp-server-part1
├── README.md
└── src
    ├── ftpserver
    │   ├── FTPServer.java      启动服务器，等待客户端
    │   └── ClientHandler.java  处理一个客户端的命令
    └── testclient
        └── TestClient.java     你自己测试用的黑窗口客户端
```

`FTPServer.java` 只负责“开门迎客”。真正干活的是 `ClientHandler.java`。

---

## 第 2 步：先理解“服务器怎么开门”

看 `FTPServer.java` 的 `main` 方法：

```java
public static void main(String[] args) throws IOException {
    new FTPServer(2121, Paths.get("server_files")).start();
}
```

意思就是：

- 端口号是 `2121`
- 服务器上的文件仓库是当前目录下的 `server_files`
- 启动后会自动创建这个文件夹

`start()` 方法里有一个无限循环：

```java
while (true) {
    Socket socket = serverSocket.accept();
    pool.submit(new ClientHandler(socket, root));
}
```

每一行可以这样理解：

- `accept()`：等一个客户端来连接
- `new ClientHandler(...)`：为这个客户端创建一个“服务员”
- `pool.submit(...)`：让这个服务员自己工作，不影响其他客户端

如果不用线程池，一个客户端卡住，其他客户端就都连不上了。

---

## 第 3 步：先理解“服务员怎么干活”

看 `ClientHandler.java` 的 `run()` 方法：

```java
while (true) {
    String command = in.readUTF();
    switch (command) {
        case "LOGIN":
            handleLogin(in, out);
            break;
        case "LIST":
            handleList(out);
            break;
        // 其他命令类似
        case "QUIT":
            return;
    }
}
```

这就是一个“命令分发器”：客户端说 `LOGIN`，你就去执行登录；客户端说 `LIST`，你就去列文件。

---

## 第 4 步：登录验证

登录功能在 `handleLogin` 方法里。

示例代码先写死一个账号：

- 用户名：`admin`
- 密码：`123456`

```java
private void handleLogin(DataInputStream in, DataOutputStream out) throws IOException {
    String username = in.readUTF();
    String password = in.readUTF();

    if ("admin".equals(username) && "123456".equals(password)) {
        loggedIn = true;
        out.writeUTF("OK 登录成功");
    } else {
        out.writeUTF("ERROR 账号或密码错误");
    }
}
```

登录成功后，`loggedIn` 变成 `true`。后面所有文件操作都要先检查它：

```java
private boolean requireLogin(DataOutputStream out) throws IOException {
    if (!loggedIn) {
        out.writeUTF("ERROR 请先登录");
        return false;
    }
    return true;
}
```

---

## 第 5 步：文件列表

列表功能在 `handleList` 方法里。

思路是：

1. 打开服务器仓库目录
2. 把每个文件的名字和大小拼成一个字符串
3. 用 `OK` 加换行返回给客户端

```java
try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
    for (Path path : stream) {
        result.append(path.getFileName())
              .append(" [")
              .append(Files.size(path))
              .append(" 字节]")
              .append(System.lineSeparator());
    }
}
```

---

## 第 6 步：上传文件

上传在 `handleUpload` 方法里。

客户端会按顺序发来：

1. 文件名
2. 文件大小
3. 文件内容

服务器这边要做的是：

```java
long size = in.readLong();
long remaining = size;

while (remaining > 0) {
    int readSize = (int) Math.min(BUFFER_SIZE, remaining);
    int count = in.read(buffer, 0, readSize);
    fileOut.write(buffer, 0, count);
    remaining -= count;
}
```

为什么要循环？

因为大文件不能一次性读进内存。每次只读一小块，再写到服务器硬盘上，这就是“分块传输”。

---

## 第 7 步：下载文件

下载在 `handleDownload` 方法里。

服务器先告诉客户端文件有多大，再开始发文件内容：

```java
out.writeUTF("OK " + Files.size(target));

while ((count = fileIn.read(buffer)) != -1) {
    out.write(buffer, 0, count);
}
```

客户端收到 `OK 文件大小` 后，就知道接下来要连续读多少个字节。

---

## 第 8 步：删除和重命名

删除在 `handleDelete` 方法里：

```java
Files.delete(target);
out.writeUTF("OK 删除成功");
```

重命名在 `handleRename` 方法里：

```java
Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
out.writeUTF("OK 重命名成功");
```

这两个功能很简单，但要注意：不能允许 `../` 这种路径跑出仓库目录。

示例代码里的 `safeResolve` 方法就是做安全校验的：

```java
Path resolved = root.toAbsolutePath().resolve(fileName).normalize();
if (!resolved.startsWith(root.toAbsolutePath().normalize())) {
    out.writeUTF("ERROR 非法文件名");
    return null;
}
```

---

## 第 9 步：如何测试

测试分两个窗口：

1. 先启动服务器
2. 再启动 `TestClient.java`

`TestClient.java` 会自动按顺序测试：

- 登录
- 查看列表
- 上传 `hello.txt`
- 再次查看列表
- 下载成 `downloaded_hello.txt`
- 重命名成 `renamed.txt`
- 删除
- 退出

如果每一步都看到 `OK`，说明你的服务端核心功能已经通了。

---

## 常见报错

### 端口被占用

`Address already in use` 表示 `2121` 端口已经被占用。

解决：关掉旧程序，或者换一个端口。

### 找不到服务器

客户端连不上时，先确认服务器窗口已经启动，再确认端口号一致。

### 中文乱码

在 IDE 里把源码编码设置成 UTF-8，并且让全队统一用 UTF-8。

---

## 答辩时可以这样讲

1. 服务器用 `ServerSocket` 监听端口，等客户端连接
2. 每个客户端单独一个处理线程，互不干扰
3. 收到命令后，用 `switch` 分发到对应方法
4. 上传下载都用分块读写，避免大文件一次读进内存
5. 用 `normalize` 和 `startsWith` 防止用户越权访问仓库外文件
6. 最后演示完整登录、列表、上传、下载、删除、重命名流程

记住：你的目标不是背代码，而是能解释每一步“为什么要这么做”。

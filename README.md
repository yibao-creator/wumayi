# FTP-Client（成员4：Swing 界面层）

## 目录结构

```
FTP-Client/
├── src/
│   ├── Main.java              # 程序入口（启动登录窗口）
│   ├── ui/
│   │   ├── LoginFrame.java    # 登录窗口：IP/端口/账号/密码
│   │   └── MainFrame.java     # 主窗口：文件列表 + 刷新/上传/下载/删除/重命名
│   └── business/
│       ├── IFtpController.java   # 接口约定（对接协议，成员3实现它）
│       ├── FakeController.java   # 假控制器（成员4本地测试用）
│       └── ControllerFactory.java # 控制器工厂（联调只改这里一行）
└── README.md
```

## 怎么运行

1. 用 IntelliJ IDEA 打开 `C:\Users\lenovo\IdeaProjects\FTP-Client`
2. 右键 `src/Main.java` → Run 'Main'（或直接运行 `ui.LoginFrame`）
3. 当前不需要服务器也能跑：`FakeController` 返回假数据
   - 密码留空 → 模拟"登录失败"
   - 随便输密码 → 登录成功，进入主窗口

## 对接说明（发给成员3 / 成员5）

### 成员3
- 实现 `business.IFtpController` 接口，方法签名不要改。
- 完成后在 `business/ControllerFactory.create()` 里把 `new FakeController()`
  换成你的实现，UI 代码一行都不用动。
- `listFiles()` 每项返回 `String[]{文件名, 大小}`，如 `{"报告.docx", "256 KB"}`。

### 成员5
- 提供 `LoginException` / `NetworkException` / `FileTransferException`。
- 还没写好前，先统一用 `Exception` 兜底，界面 `catch (Exception e)` 即可。

## 答辩要点

- 界面布局设计（登录窗口 GridLayout、主窗口 BorderLayout + JTable）
- 按钮事件只调用业务层接口，体现分层解耦
- 所有网络操作用 SwingWorker 放后台线程，界面不卡死
- 演示脚本：登录 → 刷新 → 上传 → 下载 → 删除 → 重命名
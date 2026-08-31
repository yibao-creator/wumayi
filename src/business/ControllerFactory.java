package business;

/**
 * 控制器工厂（成员4维护）。
 *
 * 联调时只需要改这一处：把 new FakeController() 换成成员3实现 IFtpController 的类，
 * UI 代码一行都不用动。
 */
public class ControllerFactory {

    public static IFtpController create() {
        // TODO 联调时改成成员3的类，例如：return new FtpController();
        return new FakeController();
    }
}

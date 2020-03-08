package cn.edu.buaa.judger.exception;

/**
 * 创建文件夹失败的IO异常
 * 当java.io.File.File.mkdirs()返回false时抛出
 */
public class CreateDirectoryException extends Exception {
    /**
     * CreateDirectoryException的构造函数
     * @param message - 错误消息
     */
    public CreateDirectoryException(String message) {
        super(message);
    }
}

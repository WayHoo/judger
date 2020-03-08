package cn.edu.buaa.judger.exception;

/**
 * 无效的提交记录异常
 * 当getSubmissionById(long)操作返回null时抛出
 */
public class IllegalSubmissionException extends Exception {
    public IllegalSubmissionException(String message){
        super(message);
    }
}

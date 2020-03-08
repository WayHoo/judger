package cn.edu.buaa.judger.model;

public class Problem {
    /**
     * 题目ID
     */
    private long problemId;

    /**
     * 题目运行最长时间限制，单位为毫秒
     */
    private int timeLimit;

    /**
     * 题目运行最大内存限制，单位为KB
     */
    private int memoryLimit;

    /**
     * 题目已有代码，用于填空题
     */
    private String code;

    public Problem() { }

    public long getProblemId() {
        return problemId;
    }

    public void setProblemId(long problemId) {
        this.problemId = problemId;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

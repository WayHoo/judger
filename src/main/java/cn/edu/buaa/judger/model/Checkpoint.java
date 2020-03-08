package cn.edu.buaa.judger.model;

public class Checkpoint {
    /**
     * 通过一个测试点所得分值
     */
    public static final int SCORE_PER_CHECKPOINT = 10;

    /**
     * 测试点ID
     */
    private long checkpointId;

    /**
     * 题目ID
     */
    private long problemId;

    /**
     * 指定题目的测试点序号
     */
    private int checkpointNumber;

    /**
     *测试点的标准输入
     */
    private String input;

    /**
     * 测试点的标准输出
     */
    private String output;

    public Checkpoint() { }

    public long getCheckpointId() {
        return checkpointId;
    }

    public void setCheckpointId(long checkpointId) {
        this.checkpointId = checkpointId;
    }

    public long getProblemId() {
        return problemId;
    }

    public void setProblemId(long problemId) {
        this.problemId = problemId;
    }

    public int getCheckpointNumber() {
        return checkpointNumber;
    }

    public void setCheckpointNumber(int checkpointNumber) {
        this.checkpointNumber = checkpointNumber;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}

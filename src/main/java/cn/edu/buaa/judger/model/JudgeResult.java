package cn.edu.buaa.judger.model;

public class JudgeResult {
    /**
     * 评测结果ID
     */
    private int judgeResultId;

    /**
     * 评测结果简称
     */
    private String judgeResultSlug;

    /**
     * 评测结果全称
     */
    private String judgeResultName;

    public JudgeResult() { }

    public int getJudgeResultId() {
        return judgeResultId;
    }

    public void setJudgeResultId(int judgeResultId) {
        this.judgeResultId = judgeResultId;
    }

    public String getJudgeResultSlug() {
        return judgeResultSlug;
    }

    public void setJudgeResultSlug(String judgeResultSlug) {
        this.judgeResultSlug = judgeResultSlug;
    }

    public String getJudgeResultName() {
        return judgeResultName;
    }

    public void setJudgeResultName(String judgeResultName) {
        this.judgeResultName = judgeResultName;
    }
}

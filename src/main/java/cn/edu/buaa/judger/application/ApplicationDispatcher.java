package cn.edu.buaa.judger.application;

import cn.edu.buaa.judger.core.Dispatcher;
import cn.edu.buaa.judger.mapper.JudgeResultMapper;
import cn.edu.buaa.judger.model.JudgeResult;
import cn.edu.buaa.judger.service.JMSProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ApplicationDispatcher {
    /**
     * 自动注入的Dispatcher对象.
     * 用于完成评测作业的任务调度.
     */
    @Autowired
    private Dispatcher judgerDispatcher;

    @Autowired
    private JMSProducerService jmsProducerService;

    @Autowired
    private JudgeResultMapper judgeResultMapper;

    /**
     * 收到消息队列的新的评测请求时的回调函数.
     * @param submissionId - 评测记录的唯一标识符
     */
    public void onSubmissionCreated(long submissionId) {
        try {
            judgerDispatcher.createNewTask(submissionId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 当系统错误发生时通知用户
     * @param submissionId - 评测记录的唯一标识符
     */
    public void onErrorOccurred(long submissionId) {
        Map<String, Object> mapMessage = new HashMap<>();
        mapMessage.put("submissionId", submissionId);
        mapMessage.put("event", "ErrorOccurred");
        mapMessage.put("log", "Internal error occured.");
        jmsProducerService.sendMessage(mapMessage);
    }

    /**
     * 当编译阶段结束时通知用户.
     * @param submissionId - 评测记录的唯一标识符
     * @param result - 编译结果
     */
    public void onCompileFinished(long submissionId, Map<String, Object> result) {
        boolean isSuccessful = (boolean)result.get("isSuccessful");
        String compileLog = getCompileLog(result);
        Map<String, Object> mapMessage = new HashMap<>();
        mapMessage.put("submissionId", submissionId);
        mapMessage.put("event", "CompileFinished");
        mapMessage.put("isSuccessful", isSuccessful);
        mapMessage.put("compileLog", compileLog);
        jmsProducerService.sendMessage(mapMessage);
    }

    /**
     * 实时返回评测结果
     * @param submissionId - 提交记录的编号
     * @param checkpointId - 测试点的编号
     * @param runtimeResult - 某个测试点的程序运行结果
     */
    public void onOneTestPointFinished(long submissionId, long checkpointId, Map<String, Object> runtimeResult) {
        String runtimeResultSlug = getRuntimeResultSlug(runtimeResult);
        int usedTime = getUsedTime(runtimeResult);
        int usedMemory = getUsedMemory(runtimeResult);
        int score = getScore(runtimeResult);
        Map<String, Object> mapMessage = new HashMap<>();
        mapMessage.put("event", "CheckpointFinished");
        mapMessage.put("submissionId", submissionId);
        mapMessage.put("checkpointId", checkpointId);
        mapMessage.put("judgeResult", runtimeResultSlug);
        mapMessage.put("usedTime", usedTime);
        mapMessage.put("usedMemory", usedMemory);
        mapMessage.put("score", score);
        jmsProducerService.sendMessage(mapMessage);
    }

    /**
     * 持久化程序评测结果
     * @param submissionId - 提交记录的编号
     * @param runtimeResults - 对各个测试点的评测结果集
     */
    public void onAllTestPointsFinished(long submissionId, List<Map<String, Object>> runtimeResults) {
        int totalTime = 0;
        int maxMemory = 0;
        int totalScore = 0;
        String runtimeResultSlug = "AC";
        for ( Map<String, Object> runtimeResult : runtimeResults ) {
            String currentRuntimeResultSlug = getRuntimeResultSlug(runtimeResult);
            int usedTime = getUsedTime(runtimeResult);
            int usedMemory = getUsedMemory(runtimeResult);
            int score = getScore(runtimeResult);
            totalTime += usedTime;
            maxMemory = usedMemory > maxMemory ? usedMemory : maxMemory;
            if ( "AC".equals(currentRuntimeResultSlug) ) {
                totalScore += score;
            }
            if ( !"AC".equals(currentRuntimeResultSlug) ) {
                runtimeResultSlug = currentRuntimeResultSlug;
            }
        }
        String runtimeLog = getRuntimeLog(runtimeResults, runtimeResultSlug, totalTime, maxMemory, totalScore);
        Map<String, Object> mapMessage = new HashMap<>();
        mapMessage.put("event", "AllTestPointsFinished");
        mapMessage.put("submissionId", submissionId);
        mapMessage.put("judgeResult", runtimeResultSlug);
        mapMessage.put("totalTime", totalTime);
        mapMessage.put("maxMemory", maxMemory);
        mapMessage.put("totalScore", totalScore);
        mapMessage.put("runtimeLog", runtimeLog);
        jmsProducerService.sendMessage(mapMessage);
    }

    /**
     * 格式化编译时日志.
     * @param result - 包含编译状态的Map<String, Object>对象
     * @return 格式化后的日志
     */
    private String getCompileLog(Map<String, Object> result) {
        boolean isSuccessful = (boolean)result.get("isSuccessful");
        String compileLog = (String)result.get("log");
        StringBuilder formatedLogBuilder = new StringBuilder();
        formatedLogBuilder.append(String.format("Compile %s.\n\n", new Object[] { isSuccessful ? "Successful" : "Error" }));
        if ( !isSuccessful ) {
            formatedLogBuilder.append(compileLog.replace("\n", "\n\n"));
            formatedLogBuilder.append("\nCompile Error, Time = 0 ms, Memory = 0 KB, Score = 0.\n");
        }
        return formatedLogBuilder.toString();
    }

    /**
     * 格式化运行时日志
     * @param runtimeResults - 对各个测试点的评测结果集
     * @param runtimeResultSlug
     * @param totalTime
     * @param maxMemory
     * @param totalScore
     * @return
     */
    private String getRuntimeLog(List<Map<String, Object>> runtimeResults,
                               String runtimeResultSlug, int totalTime,
                               int maxMemory, int totalScore) {
        long checkpointId = -1;
        String runtimeResultName = getRuntimeResultName(runtimeResultSlug);
        StringBuilder formatedLogBuilder = new StringBuilder();
        formatedLogBuilder.append("Compile Successfully.\n\n");
        for ( Map<String, Object> runtimeResult : runtimeResults ) {
            String currentRuntimeResultSlug = getRuntimeResultSlug(runtimeResult);
            String currentRuntimeResultName = getRuntimeResultName(currentRuntimeResultSlug);
            int usedTime = getUsedTime(runtimeResult);
            int usedMemory = getUsedMemory(runtimeResult);
            int score = getScore(runtimeResult);
            if ( !"AC".equals(currentRuntimeResultSlug) ) {
                score = 0;
            }
            formatedLogBuilder.append(String.format("- Test Point #%d: %s, Time = %d ms, Memory = %d KB, Score = %d\n",
                    new Object[] { ++ checkpointId, currentRuntimeResultName, usedTime, usedMemory, score }));
        }
        formatedLogBuilder.append(String.format("\n%s, Time = %d ms, Memory = %d KB, Score = %d\n",
                new Object[] { runtimeResultName, totalTime, maxMemory, totalScore }));
        return formatedLogBuilder.toString();
    }

    /**
     * 从评测结果集中获取程序评测结果的唯一英文缩写
     * @param runtimeResult - 程序评测结果
     * @return 程序评测结果的唯一英文缩写
     */
    private String getRuntimeResultSlug(Map<String, Object> runtimeResult) {
        Object runtimeResultObject = runtimeResult.get("runtimeResult");
        if ( runtimeResultObject == null ) {
            return "SE";
        }
        return (String)runtimeResultObject;
    }

    /**
     * 获取评测结果的全称.
     * @param runtimeResultSlug - 评测结果的唯一英文缩写
     * @return 评测结果的全称
     */
    private String getRuntimeResultName(String runtimeResultSlug) {
        JudgeResult judgeResult = judgeResultMapper.getJudgeResultBySlug(runtimeResultSlug);
        if ( judgeResult == null ) {
            return "System Error";
        }
        return judgeResult.getJudgeResultName();
    }

    /**
     * 从评测结果集中获取程序运行时间(ms)
     * @param runtimeResult - 程序评测结果
     * @return 程序运行时间(ms)
     */
    private int getUsedTime(Map<String, Object> runtimeResult) {
        Object usedTimeObject = runtimeResult.get("usedTime");
        if ( usedTimeObject == null ) {
            return 0;
        }
        return (int)usedTimeObject;
    }

    /**
     * 从评测结果集中获取内存使用量(KB)
     * @param runtimeResult - 程序评测结果
     * @return 内存使用量(KB)
     */
    private int getUsedMemory(Map<String, Object> runtimeResult) {
        Object usedMemoryObject = runtimeResult.get("usedMemory");
        if ( usedMemoryObject == null ) {
            return 0;
        }
        return (int)usedMemoryObject;
    }

    /**
     * 从评测结果集中获取测试点对应的分值
     * @param runtimeResult - 程序评测结果
     * @return 测试点对应的分值
     */
    private int getScore(Map<String, Object> runtimeResult) {
        Object scoreObject = runtimeResult.get("score");
        if ( scoreObject == null ) {
            return 0;
        }
        return (int)scoreObject;
    }


    /**
     * 将评测结果封装为Map格式的数据
     * @param submissionId - 提交记录的唯一标识符
     * @param usedTime - 提交运行使用时间(所有时间之和)
     * @param usedMemory - 提交运行使用内存(最大内存占用)
     * @param score - 运行得分
     * @param judgeResult - 运行结果
     * @param compileOutput - 编译输出(或运行日志记录)
     */
    private Map<String,Object> wrapJudgeResult2MapMessage(long submissionId, int usedTime,
                                  int usedMemory, int score, String judgeResult, String compileOutput) {
        Map<String,Object> mapMessage = new HashMap<>();
        mapMessage.put("submissionId", submissionId);
        mapMessage.put("usedTime", usedTime);
        mapMessage.put("usedMemory", usedMemory);
        mapMessage.put("score", score);
        mapMessage.put("judgeResult", judgeResult);
        mapMessage.put("compileOutput", compileOutput);
        return mapMessage;
    }
}

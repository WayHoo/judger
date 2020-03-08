package cn.edu.buaa.judger.core;

import cn.edu.buaa.judger.application.ApplicationDispatcher;
import cn.edu.buaa.judger.exception.IllegalSubmissionException;
import cn.edu.buaa.judger.mapper.CheckpointMapper;
import cn.edu.buaa.judger.mapper.SubmissionMapper;
import cn.edu.buaa.judger.model.Checkpoint;
import cn.edu.buaa.judger.model.Submission;
import cn.edu.buaa.judger.utils.RandomStringUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class Dispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dispatcher.class);

    /**
     * 评测机的工作目录
     * 用于存储编译结果以及程序输出结果
     */
    @Value("${judger.workDir}")
    private String workBaseDirectory;

    /**
     * 测试点的存储目录
     * 用于存储测试点的输入输出数据
     */
    @Value("${judger.checkpointDir}")
    private String checkpointDirectory;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private ApplicationDispatcher applicationDispatcher;

    @Autowired
    private Compiler compiler;

    @Autowired
    private Comparator comparator;

    /**
     * 自动注入的Preprocessor对象
     * 完成编译前的准备工作
     */
    @Autowired
    private Preprocessor preprocessor;

    @Autowired
    private CheckpointMapper checkpointMapper;

    @Autowired
    private Runner runner;

    public void createNewTask(long submissionId) throws IllegalSubmissionException {
        synchronized (this) {
            String baseDirectory = String.format("%s/buaaoj-%s", new Object[] {workBaseDirectory, submissionId});
            String baseFileName = RandomStringUtil.getRandomString(12, RandomStringUtil.Mode.ALPHA);
            Submission submission = submissionMapper.getSubmissionById(submissionId);
            if( submission == null ){
                throw new IllegalSubmissionException("Illegal submission #" + submissionId);
            }
            preprocess(submission, baseDirectory, baseFileName);
            if ( compile(submission, baseDirectory, baseFileName) ) {
                runProgram(submission, baseDirectory, baseFileName);
            }
            cleanUp(baseDirectory);
        }
    }

    /**
     * 完成评测前的预处理工作
     * 说明: 随机文件名用于防止应用程序自身递归调用
     * @param submission - 评测记录对象
     * @param workDirectory - 用于产生编译输出的目录
     * @param baseFileName - 随机文件名(不包含后缀)
     */
    private void preprocess(Submission submission,
                            String workDirectory, String baseFileName) {
        try {
            long problemId = submission.getProblemId();
            preprocessor.createTestCode(submission, workDirectory, baseFileName);
            preprocessor.fetchTestPoints(problemId);
        } catch (Exception ex) {
            ex.printStackTrace();
            applicationDispatcher.onErrorOccurred(submission.getSubmissionId());
        }
    }

    /**
     * 创建编译任务.
     * 说明: 随机文件名用于防止应用程序自身递归调用.
     * @param submission - 评测记录对象
     * @param workDirectory - 用于产生编译输出的目录
     * @param baseFileName - 随机文件名(不包含后缀)
     */
    private boolean compile(Submission submission,
                            String workDirectory, String baseFileName) {
        long submissionId = submission.getSubmissionId();
        Map<String, Object> result =
                compiler.getCompileResult(submission, workDirectory, baseFileName);
        applicationDispatcher.onCompileFinished(submissionId, result);
        return (boolean)result.get("isSuccessful");
    }

    /**
     * 执行程序
     * @param submission - 评测记录对象
     * @param workDirectory - 编译生成结果的目录以及程序输出的目录
     * @param baseFileName - 待执行的应用程序文件名(不包含文件后缀)
     */
    private void runProgram(Submission submission,
                            String workDirectory, String baseFileName) {
        List<Map<String, Object>> runtimeResults = new ArrayList<>();
        long submissionId = submission.getSubmissionId();
        long problemId = submission.getProblemId();
        List<Checkpoint> checkpoints = checkpointMapper.getProblemCheckpoints(problemId);
        for ( Checkpoint checkpoint : checkpoints ) {
            long checkpointId = checkpoint.getCheckpointId();
            String inputFilePath = String.format("%s/%s/input#%s.txt",
                    new Object[] { checkpointDirectory, problemId, checkpointId });
            String stdOutputFilePath = String.format("%s/%s/output#%s.txt",
                    new Object[] { checkpointDirectory, problemId, checkpointId });
            String outputFilePath = getOutputFilePath(workDirectory, checkpointId);
            Map<String, Object> runtimeResult = getRuntimeResult(
                    runner.getRuntimeResult(submission, workDirectory, baseFileName, inputFilePath, outputFilePath),
                    stdOutputFilePath, outputFilePath);
            runtimeResult.put("score", Checkpoint.SCORE_PER_CHECKPOINT);
            runtimeResults.add(runtimeResult);
//            applicationDispatcher.onOneTestPointFinished(submissionId, checkpointId, runtimeResult);
        }
        applicationDispatcher.onAllTestPointsFinished(submissionId, runtimeResults);
    }

    /**
     * 获取当前测试点输出路径
     * @param workDirectory - 编译生成结果的目录以及程序输出的目录
     * @param checkpointId - 当前测试点编号
     * @return 当前测试点输出路径
     */
    private String getOutputFilePath(String workDirectory, long checkpointId) {
        return String.format("%s/output#%s.txt",
                new Object[] {workDirectory, checkpointId});
    }

    /**
     * 获取程序运行结果(及答案比对结果)
     * @param result - 包含程序运行结果的Map对象
     * @param standardOutputFilePath - 标准输出文件路径
     * @param outputFilePath - 用户输出文件路径
     * @return 包含程序运行结果的Map对象
     */
    private Map<String, Object> getRuntimeResult(Map<String, Object> result,
                                                 String standardOutputFilePath, String outputFilePath) {
        String runtimeResultSlug = (String)result.get("runtimeResult");
        int usedTime = (int)result.get("usedTime");
        int usedMemory = (int)result.get("usedMemory");
        if ( runtimeResultSlug.equals("AC") &&
                !isOutputTheSame(standardOutputFilePath, outputFilePath) ) {
            runtimeResultSlug = "WA";
            result.put("runtimeResult", runtimeResultSlug);
        }
        LOGGER.info(String.format("RuntimeResult: [%s, Time: %d ms, Memory: %d KB]",
                new Object[] { runtimeResultSlug, usedTime, usedMemory }));
        return result;
    }

    /**
     * 获取用户输出和标准输出的比对结果.
     * @param standardOutputFilePath - 标准输出文件路径
     * @param outputFilePath - 用户输出文件路径
     * @return 用户输出和标准输出是否相同
     */
    private boolean isOutputTheSame(String standardOutputFilePath, String outputFilePath) {
        try {
            return comparator.isOutputTheSame(standardOutputFilePath, outputFilePath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 评测完成后, 清理所生成的文件
     * @param baseDirectory - 用于产生输出结果目录
     */
    private void cleanUp(String baseDirectory) {
        File baseDirFile = new File(baseDirectory);
        if ( baseDirFile.exists() ) {
            try {
                FileUtils.deleteDirectory(baseDirFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

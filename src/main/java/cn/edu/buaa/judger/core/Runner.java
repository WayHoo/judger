package cn.edu.buaa.judger.core;

import cn.edu.buaa.judger.mapper.LanguageMapper;
import cn.edu.buaa.judger.mapper.ProblemMapper;
import cn.edu.buaa.judger.model.Language;
import cn.edu.buaa.judger.model.Problem;
import cn.edu.buaa.judger.model.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Runner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Runner.class);

    /**
     * 登录操作系统的用户名.
     * 为了安全, 我们建议评测程序以低权限的用户运行.
     */
    @Value("${system.username}")
    private String systemUsername;

    /**
     * 登录操作系统的密码.
     * 为了安全, 我们建议评测程序以低权限的用户运行.
     */
    @Value("${system.password}")
    private String systemPassword;

    @Autowired
    private LanguageMapper languageMapper;

    @Autowired
    private ProblemMapper problemMapper;


    /**
     * 获取程序运行（编译）结果.
     * @param commandLine - 待执行程序的命令行
     * @param inputFilePath - 输入文件路径(可为NULL)
     * @param outputFilePath - 输出文件路径(可为NULL)
     * @param timeLimit - 时间限制(单位ms, 0表示不限制)
     * @param memoryLimit - 内存限制(单位KB, 0表示不限制)
     * @return 一个包含程序运行结果的Map<String, Object>对象
     */
    public Map<String, Object> getRuntimeResult(String commandLine, String inputFilePath,
                                                String outputFilePath, int timeLimit, int memoryLimit) {
        Map<String, Object> result = null;
        try {
            result = getRuntimeResult(commandLine, systemUsername, systemPassword,
                    inputFilePath, outputFilePath, timeLimit, memoryLimit);
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * 获取(用户)程序运行结果.
     *
     * @param submission - 评测记录对象
     * @param workDirectory - 编译生成结果的目录以及程序输出的目录
     * @param baseFileName - 待执行的应用程序文件名(不包含文件后缀)
     * @param inputFilePath - 输入文件路径
     * @param outputFilePath - 输出文件路径
     * @return 一个包含程序运行结果的Map<String, Object>对象
     */
    public Map<String, Object> getRuntimeResult(Submission submission, String workDirectory,
                                                String baseFileName, String inputFilePath, String outputFilePath) {
        String commandLine = getRunCommand(submission, workDirectory, baseFileName);
        int timeLimit = getTimeLimit(submission);
        int memoryLimit = getMemoryLimit(submission);
        Map<String, Object> result = new HashMap<>();
        String runtimeResultSlug = "SE";
        int usedTime = 0;
        int usedMemory = 0;
        try {
            LOGGER.info(String.format("[Submission #%d] Start running with command %s (TimeLimit=%d, MemoryLimit=%s)",
                    new Object[] { submission.getSubmissionId(), commandLine, timeLimit, memoryLimit }));
            Map<String, Object> runtimeResult = getRuntimeResult(commandLine,
                    systemUsername, systemPassword, inputFilePath, outputFilePath,
                    timeLimit, memoryLimit);
            int exitCode = (int) runtimeResult.get("exitCode");
            usedTime = (int) runtimeResult.get("usedTime");
            usedMemory = (int) runtimeResult.get("usedMemory");
            runtimeResultSlug = getRuntimeResultSlug(exitCode, timeLimit, usedTime, memoryLimit, usedMemory);
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        result.put("runtimeResult", runtimeResultSlug);
        result.put("usedTime", usedTime);
        result.put("usedMemory", usedMemory);
        return result;
    }

    /**
     * 获取程序运行（编译）结果.
     * @param commandLine - 待执行程序的命令行
     * @param systemUsername - 登录操作系统的用户名
     * @param systemPassword - 登录操作系统的密码
     * @param inputFilePath - 输入文件路径(可为NULL)
     * @param outputFilePath - 输出文件路径(可为NULL)
     * @param timeLimit - 时间限制(单位ms, 0表示不限制)
     * @param memoryLimit - 内存限制(单位KB, 0表示不限制)
     * @return 一个包含程序运行结果的Map<String, Object>对象
     */
    public native Map<String, Object> getRuntimeResult(String commandLine, String systemUsername,
                                                       String systemPassword, String inputFilePath,
                                                       String outputFilePath, int timeLimit, int memoryLimit);

    /**
     * 获取待执行的命令行
     * @param submission - 评测记录对象
     * @param workDirectory - 编译生成结果的目录以及程序输出的目录
     * @param baseFileName - 待执行的应用程序文件名(不包含文件后缀)
     * @return 待执行的命令行
     */
    private String getRunCommand(Submission submission,
                                 String workDirectory, String baseFileName) {
        Language language = languageMapper.getLanguageById(submission.getLanguageId());
        String filePathWithoutExtension = String.format("%s/%s",
                new Object[] {workDirectory, baseFileName});
        StringBuilder runCommand = new StringBuilder(language.getRunCommand()
                .replaceAll("\\{filename\\}", filePathWithoutExtension));
        //--------------------------------------------------------------
//        if ( language.getLanguageName().equalsIgnoreCase("Java") ) {
//            int lastIndexOfSpace = runCommand.lastIndexOf("/");
//            runCommand.setCharAt(lastIndexOfSpace, ' ');
//        }
        //--------------------------------------------------------------
        return runCommand.toString();
    }

    /**
     * 根据不同语言获取最大时间限制
     * @param submission - 评测记录对象
     * @return 最大时间限制
     */
    private int getTimeLimit(Submission submission) {
        Problem problem = problemMapper.getProblemById(submission.getProblemId());
        Language language = languageMapper.getLanguageById(submission.getLanguageId());
        int timeLimit = problem.getTimeLimit();
        if ( language.getLanguageName().equalsIgnoreCase("Java") ) {
            timeLimit *= 2;
        }
        return timeLimit;
    }

    /**
     * 根据不同语言获取最大空间限制.
     * @param submission - 评测记录对象
     * @return 最大空间限制
     */
    private int getMemoryLimit(Submission submission) {
        Problem problem = problemMapper.getProblemById(submission.getProblemId());
        int memoryLimit = problem.getMemoryLimit();
        return memoryLimit;
    }

    /**
     * 根据JNI返回的结果封装评测结果
     * @param exitCode - 程序退出状态位
     * @param timeLimit - 最大时间限制
     * @param timeUsed - 程序运行所用时间
     * @param memoryLimit - 最大空间限制
     * @param memoryUsed - 程序运行所用空间(最大值)
     * @return 程序运行结果的唯一英文缩写
     */
    private String getRuntimeResultSlug(int exitCode, int timeLimit, int timeUsed, int memoryLimit, int memoryUsed) {
        if ( exitCode == 0 ) {
            // Output will be compared in next stage
            return "AC";
        }
        if ( timeUsed >= timeLimit ) {
            return "TLE";
        }
        if ( memoryUsed >= memoryLimit ) {
            return "MLE";
        }
        return "RE";
    }
}
package cn.edu.buaa.judger.core;

import cn.edu.buaa.judger.mapper.LanguageMapper;
import cn.edu.buaa.judger.model.Language;
import cn.edu.buaa.judger.model.Submission;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 程序编译器, 用于编译用户提交的代码
 */
@Component
public class Compiler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Compiler.class);

    @Autowired
    private LanguageMapper languageMapper;

    /**
     * 自动注入的Runner对象.
     * 用于执行编译命令.
     */
    @Autowired
    private Runner compilerRunner;

    /**
     * 获取编译输出结果
     * @param submission - 提交记录对象
     * @param workDirectory - 编译输出目录
     * @param baseFileName - 编译输出文件名
     * @return 包含编译输出结果的Map<String, Object>对象
     */
    public Map<String, Object> getCompileResult(Submission submission,
                                                String workDirectory, String baseFileName) {
        String compileCommand = getCompileCommand(submission, workDirectory, baseFileName);
        String compileLogPath = getCompileLogPath(workDirectory, baseFileName);
        return getCompileResult(compileCommand, compileLogPath);
    }

    /**
     * 获取编译命令
     * @param submission - 提交记录对象
     * @param workDirectory - 编译输出目录
     * @param baseFileName - 编译输出文件名
     * @return 编译命令
     */
    private String getCompileCommand(Submission submission,
                                     String workDirectory, String baseFileName) {
        String filePathWithoutExtension = String.format("%s/%s",
                new Object[] {workDirectory, baseFileName});
        Language language = languageMapper.getLanguageById(submission.getLanguageId());
        String compileCommand = language.getCompileCommand()
                .replaceAll("\\{filename\\}", filePathWithoutExtension);
        return compileCommand;
    }

    /**
     * 获取编译日志输出的文件路径.
     * @param workDirectory - 编译输出目录
     * @param baseFileName - 编译输出文件名
     * @return 编译日志输出的文件路径
     */
    private String getCompileLogPath(String workDirectory, String baseFileName) {
        return String.format("%s/%s-compile.log",
                new Object[] {workDirectory, baseFileName});
    }

    /**
     * 获取编译输出结果.
     * @param compileCommand - 编译命令
     * @param compileLogPath - 编译日志输出路径
     * @return 包含编译输出结果的Map<String, Object>对象
     */
    private Map<String, Object> getCompileResult(String compileCommand, String compileLogPath) {
        //编译时无输入样例测试文件
        String inputFilePath = null;
        //编译时间限制
        int timeLimit = 5000;
        //编译时内存占用限制，0表示无限制
        int memoryLimit = 0;
        LOGGER.info("Start compiling with command: " + compileCommand);
        Map<String, Object> runningResult = compilerRunner.getRuntimeResult(
                compileCommand, inputFilePath, compileLogPath, timeLimit, memoryLimit);
        Map<String, Object> result = new HashMap<>(3, 1);
        boolean isSuccessful = false;
        if ( runningResult != null ) {
            int exitCode = (int)runningResult.get("exitCode");
            isSuccessful = (exitCode == 0);
        }
        result.put("isSuccessful", isSuccessful);
        result.put("log", getCompileOutput(compileLogPath));
        return result;
    }

    /**
     * 获取编译日志内容.
     * @param compileLogPath - 编译日志路径
     * @return 编译日志内容
     */
    private String getCompileOutput(String compileLogPath) {
        FileInputStream inputStream = null;
        String compileLog = "";
        try {
            inputStream = new FileInputStream(compileLogPath);
            compileLog = IOUtils.toString(inputStream, "UTF-8");
            inputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return compileLog;
    }
}

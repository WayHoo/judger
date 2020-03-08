package cn.edu.buaa.judger.application;

import cn.edu.buaa.judger.mapper.LanguageMapper;
import cn.edu.buaa.judger.model.Language;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;

/**
 * 程序评测模块的加载器
 */
public class ApplicationBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationBootstrap.class);

    @Resource
    private LanguageMapper languageMapper;

    /**
     * 应用程序入口.
     */
    public static void main(String[] args) {
        LOGGER.info("Starting BUAA Online Judge Judger...");
        ApplicationBootstrap app = new ApplicationBootstrap();
        app.getSystemEnvironment();
        app.setUpShutdownHook();
        LOGGER.info("BUAA Online Judge Judger started.");
    }

    /**
     * 获取系统环境变量.
     * 以便进行Bug的复现.
     */
    private void getSystemEnvironment() {
        LOGGER.info("System Information: " );
        LOGGER.info("\tOperating System Name: " + System.getProperty("os.name"));
        LOGGER.info("\tOperating System Version: " + System.getProperty("os.version"));
        LOGGER.info("\tJava VM Name: " + System.getProperty("java.vm.name"));
        LOGGER.info("\tJava Runtime Version: " + System.getProperty("java.runtime.version"));
        LOGGER.info("Compiler Information: " );
        List<Language> languages = languageMapper.getAllLanguages();
        LOGGER.info("languages: " + languages);
        for ( Language language : languages ) {
            String languageName = language.getLanguageName();
            String compileProgram = getCompileProgram(language.getCompileCommand());
            LOGGER.info("\t" + languageName + ": " + getCompilerVersion(languageName, compileProgram));
        }
    }

    /**
     * 设置ShutdownHook.
     * 用于完成程序正常退出前的准备工作.
     */
    private void setUpShutdownHook() {
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    LOGGER.info("BUAA Online Judge Judger is shutting down...");
                    mainThread.join();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    LOGGER.info("Something wrong happened while BUAA Online Judge Judger is shutting down!");
                }
            }
        });
    }

    /**
     * 获取编译程序的命令行.
     * @param compileCommand - 编译命令的命令行
     * @return 编译程序的命令行
     */
    private String getCompileProgram(String compileCommand) {
        int firstSpaceIndex = compileCommand.indexOf(" ");
        String compileProgram = compileCommand.substring(0, firstSpaceIndex);
        if ( "javac".equalsIgnoreCase(compileProgram) ) {
            return "java";
        }
        return compileProgram;
    }

    /**
     * 获取编译器的版本信息.
     * @param languageName - 编程语言名称
     * @param compileProgram - 编译所使用的命令
     * @return 编译器的版本信息
     */
    private String getCompilerVersion(String languageName, String compileProgram) {
        StringBuilder compilerVersion = new StringBuilder();
        try {
            //获取编译器版本信息的命令行
            String command = compileProgram + " --version";
            Process process = Runtime.getRuntime().exec(command);
            compilerVersion.append("Command Line: " + command + "\n");
            compilerVersion.append(IOUtils.toString(process.getInputStream(), "UTF-8"));
            compilerVersion.append(IOUtils.toString(process.getErrorStream(), "UTF-8"));
        } catch ( Exception ex ) {
            return "Not Found";
        }
        return compilerVersion.toString();
    }

}

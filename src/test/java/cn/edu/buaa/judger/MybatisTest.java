package cn.edu.buaa.judger;

import cn.edu.buaa.judger.application.ApplicationBootstrap;
import cn.edu.buaa.judger.mapper.LanguageMapper;
import cn.edu.buaa.judger.model.Language;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;

public class MybatisTest extends JudgerApplicationTests {
    @Resource
    private LanguageMapper languageMapper;

    @Test
    public void languageTest(){
        List<Language> languages = languageMapper.getAllLanguages();
        for (Language language : languages) {
            System.out.println(language);
        }
    }
}

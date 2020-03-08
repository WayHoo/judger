package cn.edu.buaa.judger;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("cn.edu.buaa.judger.mapper")
@EnableCaching
@ComponentScan(basePackages = {"cn.edu.buaa.judger.mapper", "cn.edu.buaa.judger.core", "cn.edu.buaa.judger.application", "cn.edu.buaa.judger.service"})
public class JudgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JudgerApplication.class, args);
    }

}

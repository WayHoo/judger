package cn.edu.buaa.judger.mapper;

import cn.edu.buaa.judger.model.JudgeResult;
import org.springframework.stereotype.Repository;

@Repository
public interface JudgeResultMapper {
    JudgeResult getJudgeResultBySlug(String judgeResultSlug);
}

package cn.edu.buaa.judger.mapper;

import cn.edu.buaa.judger.model.Problem;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemMapper {
    Problem getProblemById(long problemId);
}

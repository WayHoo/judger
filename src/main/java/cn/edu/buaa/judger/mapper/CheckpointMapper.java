package cn.edu.buaa.judger.mapper;

import cn.edu.buaa.judger.model.Checkpoint;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckpointMapper {
    List<Checkpoint> getProblemCheckpoints(long problemId);
}

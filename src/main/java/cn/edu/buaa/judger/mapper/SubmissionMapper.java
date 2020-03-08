package cn.edu.buaa.judger.mapper;

import cn.edu.buaa.judger.model.Submission;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionMapper {
    Submission getSubmissionById(long submissionId);
    void updateSubmission(Submission submission);
}

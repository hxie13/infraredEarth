package cn.ac.sitp.infrared.service.impl;

import cn.ac.sitp.infrared.datasource.dao.Algorithm;
import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.dao.Job;
import cn.ac.sitp.infrared.datasource.mapper.JobMapper;
import cn.ac.sitp.infrared.service.JobService;
import cn.ac.sitp.infrared.util.Util;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

    private final JobMapper jobMapper;

    @Override
    public Map<String, Object> getJobList(int currPage, int pageSize, AxrrAccount user) {
        Map<String, Object> contents = new HashMap<>();
        List<Job> jobList = jobMapper.selectJobList(pageSize * (currPage - 1), pageSize, user);
        contents.put("jobList", jobList);
        Util.buildPageModel(pageSize, jobMapper.selectJobCount(user), contents);
        return contents;
    }

    @Override
    public Long addJob(Long dataSetId, Long algorithmId, AxrrAccount user) {
        return jobMapper.insertJob(dataSetId, algorithmId, user);
    }

    @Override
    public Long addJobWithParams(Long dataSetId, Long algorithmId, AxrrAccount user, String parameters) {
        return jobMapper.insertJobWithParams(dataSetId, algorithmId, user, parameters);
    }

    @Override
    public Map<String, Object> getAlgorithmList(int currPage, int pageSize) {
        Map<String, Object> contents = new HashMap<>();
        List<Algorithm> algorithmList = jobMapper.selectAlgorithmList(pageSize * (currPage - 1), pageSize);
        contents.put("algorithmList", algorithmList);
        Util.buildPageModel(pageSize, jobMapper.selectAlgorithmCount(), contents);
        return contents;
    }

    @Override
    public List<Algorithm> getAlgorithmsByCategory(String category) {
        return jobMapper.selectAlgorithmsByCategory(category);
    }

    @Override
    public List<Algorithm> getAllActiveAlgorithms() {
        return jobMapper.selectAllActiveAlgorithms();
    }

    @Override
    @Async
    public void simulateJobExecution(Long jobId) {
        try {
            // Mark as running
            jobMapper.updateJobStatus(jobId, "RUN", null, null);
            log.info("Job {} started execution (simulated)", jobId);

            // Simulate processing time
            Thread.sleep(5000);

            // Mark as finished
            jobMapper.updateJobStatus(jobId, "FIN", "/output/job_" + jobId + "_result.nc", null);
            log.info("Job {} completed (simulated)", jobId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            jobMapper.updateJobStatus(jobId, "FAI", null, "Job execution interrupted");
        } catch (Exception e) {
            log.error("Job {} failed", jobId, e);
            jobMapper.updateJobStatus(jobId, "FAI", null, e.getMessage());
        }
    }
}

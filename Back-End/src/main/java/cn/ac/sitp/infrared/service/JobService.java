package cn.ac.sitp.infrared.service;

import cn.ac.sitp.infrared.datasource.dao.Algorithm;
import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;

import java.util.List;
import java.util.Map;

public interface JobService {

    Map<String, Object> getJobList(int currPage, int pageSize, AxrrAccount user);

    Long addJob(Long dataSetId, Long algorithmId, AxrrAccount user);

    Long addJobWithParams(Long dataSetId, Long algorithmId, AxrrAccount user, String parameters);

    Map<String, Object> getAlgorithmList(int currPage, int pageSize);

    List<Algorithm> getAlgorithmsByCategory(String category);

    List<Algorithm> getAllActiveAlgorithms();

    void simulateJobExecution(Long jobId);
}

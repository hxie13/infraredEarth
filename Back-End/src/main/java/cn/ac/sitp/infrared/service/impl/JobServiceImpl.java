package cn.ac.sitp.infrared.service.impl;

import cn.ac.sitp.infrared.datasource.dao.Algorithm;
import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.dao.Job;
import cn.ac.sitp.infrared.datasource.mapper.JobMapper;
import cn.ac.sitp.infrared.service.JobService;
import cn.ac.sitp.infrared.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobServiceImpl implements JobService {
    private static final Log LOG = LogFactory.getLog(JobServiceImpl.class);

    @Autowired
    private JobMapper jobMapper;

    @Override
    public Map<String, Object> getJobList(int currPage, int pageSize, AxrrAccount user) {
        Map<String, Object> contents = new HashMap<>();
        List<Job> jobList = jobMapper.selectJobList(pageSize * (currPage - 1), pageSize, user);
        contents.put("jobList", jobList);
        Util.buildPageModel(pageSize, jobMapper.selectJobCount(user), contents);
        return contents;
    }

    @Override
    public void addJob(Long dataSetId, Long algorithmId, AxrrAccount user) {
        jobMapper.insertJob(dataSetId, algorithmId, user);
    }

    @Override
    public Map<String, Object> getAlgorithmList(int currPage, int pageSize) {
        Map<String, Object> contents = new HashMap<>();
        List<Algorithm> algorithmList = jobMapper.selectAlgorithmList(pageSize * (currPage - 1), pageSize);
        contents.put("algorithmList", algorithmList);
        Util.buildPageModel(pageSize, jobMapper.selectAlgorithmCount(), contents);
        return contents;
    }
}

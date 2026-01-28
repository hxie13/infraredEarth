package cn.ac.sitp.infrared.service;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;

import java.util.Date;
import java.util.Map;

public interface JobService {

    Map<String, Object> getJobList(int currPage, int pageSize, AxrrAccount user);

    void addJob(Long dataSetId, Long algorithmId, AxrrAccount user);

    Map<String, Object> getAlgorithmList(int currPage, int pageSize);
}

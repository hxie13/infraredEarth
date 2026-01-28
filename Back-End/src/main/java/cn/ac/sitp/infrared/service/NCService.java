package cn.ac.sitp.infrared.service;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface NCService {

    Map<String, Object> getNCList(int currPage, int pageSize, String name, String title, Integer bandNumber, String regionName,
                                  String satelliteType, String resolution, String imgType, String processType, Date beginDate,
                                  Date endDate);

    void addDataset(List<Long> ncIdList, AxrrAccount user);

    InputStream getNcFileStream(Long id) throws Exception ;

    Map<String, Object> getNCTypeList();
}

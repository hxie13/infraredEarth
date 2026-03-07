package cn.ac.sitp.infrared.service;

import java.util.Date;
import java.util.Map;

public interface NaturalDisasterService {

    Map<String, Object> getNaturalDisasterList(int currPage, int pageSize, Date beginDate, Date endDate, String country,
                                               String place, String type);

    Map<String, Object> getNaturalDisasterFileData(Long id, Integer imgType) throws Exception;

    Map<String, Object> getNaturalDisasterTypeList();
}

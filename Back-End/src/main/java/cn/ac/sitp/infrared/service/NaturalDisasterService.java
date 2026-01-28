package cn.ac.sitp.infrared.service;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public interface NaturalDisasterService {

    Map<String, Object> getNaturalDisasterList(int currPage, int pageSize, Date beginDate, Date endDate, String country,
                                               String place, String type);

    Map<String, Object> getNaturalDisasterFileStream(Long id, Integer imgType) throws Exception ;

    Map<String, Object> getNaturalDisasterTypeList();
}

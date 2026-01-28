package cn.ac.sitp.infrared.service;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;

import java.util.Date;
import java.util.Map;

public interface AuditLogService {

    void saveAccountAuditLog(String ip, LogActionEnum action, String status, Date receivedTime, String userid,
                             String username, String exception);

    void saveAuditLogDesc(String ip, LogActionEnum action, String status, Date receivedTime, String exception,
                          String hospitalCode, String patid, String patname, String patsex, Date patdob, String description, AxrrAccount account);

    Map<String, Object> getLogList(int currPage, int pageSize, Date beginDate, Date endDate);
}

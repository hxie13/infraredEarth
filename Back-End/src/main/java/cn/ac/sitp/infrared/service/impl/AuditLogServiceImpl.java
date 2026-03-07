package cn.ac.sitp.infrared.service.impl;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.dao.AxrrAuditEvent;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
import cn.ac.sitp.infrared.datasource.mapper.GlobalLogMapper;
import cn.ac.sitp.infrared.service.AuditLogService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    private static final Log LOG = LogFactory.getLog(AuditLogServiceImpl.class);

    @Autowired
    private GlobalLogMapper logMapper;


    @Override
    public void saveAuditLogDesc(String ip, LogActionEnum action, String status, Date receivedTime, String exception,
                                 String hospitalCode, String patid, String patname, String patsex, Date patdob, String description,
                                 AxrrAccount account) {

        AxrrAuditEvent log = new AxrrAuditEvent();
        log.setEvent_name(action.toString());
        log.setClient_ip(ip);
        if (account == null) {
            log.setUser_id("anonymous");
            log.setUser_name("anonymous");
            log.setDisplay_name("anonymous");
        } else {
            log.setUser_id(account.getUserid());
            log.setUser_name(account.getUsername());
            log.setDisplay_name(account.getDisplayname());
        }
        log.setDescription(description);
        log.setStatus(status);
        log.setDt_received(receivedTime);
        log.setException(exception);
        logMapper.insertAuditLog(log);
    }

    @Override
    public Map<String, Object> getLogList(int currPage, int pageSize, Date beginDate, Date endDate) {
        Map<String, Object> contents = new HashMap<>();
        List<AxrrAuditEvent> logList = logMapper.selectLogList(pageSize * (currPage - 1), pageSize, beginDate, endDate);
        contents.put("logList", logList);
        return contents;
    }

    @Override
    public void saveAuditLog(String ip, LogActionEnum action, String status, Date receivedTime, String exception,
                             String description, AxrrAccount account) {
        saveAuditLogDesc(ip, action, status, receivedTime, exception, null, null, null, null, null, description, account);
    }

    @Override
    public void saveAccountAuditLog(String ip, LogActionEnum action, String status, Date receivedTime, String userid,
                                    String username, String exception) {

        AxrrAuditEvent event = new AxrrAuditEvent();
        event.setEvent_name(action.toString());
        event.setClient_ip(ip);
        event.setUser_id(userid);
        event.setUser_name(username);
        event.setDescription(action.getDescription());
        event.setStatus(status);
        event.setDt_received(receivedTime);
        event.setDt_responsed(new Date());
        event.setException(exception);
        logMapper.insertAuditLog(event);

    }

}

package cn.ac.sitp.infrared.service.impl;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.dao.AxrrAuditEvent;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
import cn.ac.sitp.infrared.datasource.mapper.GlobalLogMapper;
import cn.ac.sitp.infrared.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final GlobalLogMapper logMapper;

    @Async
    @Override
    public void saveAuditLogDesc(String ip, LogActionEnum action, String status, Date receivedTime, String exception,
                                 String hospitalCode, String patid, String patname, String patsex, Date patdob, String description,
                                 AxrrAccount account) {

        AxrrAuditEvent event = new AxrrAuditEvent();
        event.setEventName(action.toString());
        event.setClientIp(ip);
        if (account == null) {
            event.setUserId("anonymous");
            event.setUserName("anonymous");
            event.setDisplayName("anonymous");
        } else {
            event.setUserId(account.getUserid());
            event.setUserName(account.getUsername());
            event.setDisplayName(account.getDisplayname());
        }
        event.setDescription(description);
        event.setStatus(status);
        event.setDtReceived(receivedTime);
        event.setException(exception);
        logMapper.insertAuditLog(event);
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

    @Async
    @Override
    public void saveAccountAuditLog(String ip, LogActionEnum action, String status, Date receivedTime, String userid,
                                    String username, String exception) {

        AxrrAuditEvent event = new AxrrAuditEvent();
        event.setEventName(action.toString());
        event.setClientIp(ip);
        event.setUserId(userid);
        event.setUserName(username);
        event.setDescription(action.getDescription());
        event.setStatus(status);
        event.setDtReceived(receivedTime);
        event.setDtResponsed(new Date());
        event.setException(exception);
        logMapper.insertAuditLog(event);
    }

}

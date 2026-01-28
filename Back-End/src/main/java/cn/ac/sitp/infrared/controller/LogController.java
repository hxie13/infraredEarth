package cn.ac.sitp.infrared.controller;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
import cn.ac.sitp.infrared.service.AuditLogService;
import cn.ac.sitp.infrared.util.Util;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping(value = "/rest/log")
public class LogController {
    private static final Logger log = LoggerFactory.getLogger(LogController.class);

    @Resource
    private HttpServletRequest request;

    @Autowired
    private AuditLogService logService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> getDeviceInfoDetail(@RequestParam(value = "curr_page", defaultValue = "1") int currPage,
                                                   @RequestParam(value = "page_size", defaultValue = "10") int pageSize,
                                                   @RequestParam(value = "begin_date", required = false) String beginDateStr,
                                                   @RequestParam(value = "end_date", required = false) String endDateStr) {
        String ip = Util.getUserIpAddr(request);
//        Subject subject = SecurityUtils.getSubject();
//        AxrrAccount user = (AxrrAccount) subject.getPrincipal();
        AxrrAccount user = new AxrrAccount();
        user.setUserno(1);
        user.setDisplayname("管理员");
        user.setUserid("admin");
        user.setUsername("admin");

        Date beginDate = null, endDate = null;
        if (!StringUtils.isEmpty(beginDateStr)) {
            beginDate = Util.strToDate(beginDateStr + " 00:00:00", Util.FORMAT_LONG);
        }
        if (!StringUtils.isEmpty(endDateStr)) {
            endDate = Util.strToDate(endDateStr + " 23:59:59", Util.FORMAT_LONG);
        }
        try {
            Map<String, Object> contents = logService.getLogList(currPage, pageSize, beginDate, endDate);
            logService.saveAuditLogDesc(ip, LogActionEnum.GET_LOG_LIST, Util.STATUS_SUCCESS, new Date(), null, null, null, null, null,
                    null, LogActionEnum.GET_LOG_LIST.getDescription() + "beginDate: " + beginDateStr + ", endDate: " + endDateStr, user);
            return Util.suc(contents);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            // msg就是最后取出来的字符串,可存数据库
            String msg = sw.toString();
            log.error("系统异常：", e);
            try {
                logService.saveAuditLogDesc(ip, LogActionEnum.GET_LOG_LIST, Util.STATUS_FAILURE, new Date(), msg, null, null, null, null,
                        null, LogActionEnum.GET_LOG_LIST.getDescription() + "beginDate: " + beginDateStr + ", endDate: " + endDateStr, user);
            } catch (Exception ex) {
                log.error("系统异常：", ex);
            }
            return Util.err(null, msg);
        }
    }


}

package cn.ac.sitp.infrared.controller;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
import cn.ac.sitp.infrared.security.SessionAccountHelper;
import cn.ac.sitp.infrared.service.AuditLogService;
import cn.ac.sitp.infrared.util.RequestValueUtils;
import cn.ac.sitp.infrared.util.Util;
import cn.ac.sitp.infrared.web.GlobalExceptionHandler;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping(value = "/rest/log")
public class LogController {

    @Resource
    private HttpServletRequest request;

    @Autowired
    private AuditLogService logService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> getDeviceInfoDetail(
            @RequestParam(value = "curr_page", defaultValue = "1") int currPage,
            @RequestParam(value = "page_size", defaultValue = "10") int pageSize,
            @RequestParam(value = "begin_date", required = false) String beginDateStr,
            @RequestParam(value = "end_date", required = false) String endDateStr) {
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        String ip = Util.getUserIpAddr(request);
        String description = LogActionEnum.GET_LOG_LIST.getDescription()
                + " beginDate: " + beginDateStr + ", endDate: " + endDateStr;
        GlobalExceptionHandler.setAuditContext(request, LogActionEnum.GET_LOG_LIST, description);

        int normalizedCurrPage = RequestValueUtils.normalizePage(currPage);
        int normalizedPageSize = RequestValueUtils.normalizePageSize(pageSize);
        Date beginDate = RequestValueUtils.parseBeginDate(beginDateStr);
        Date endDate = RequestValueUtils.parseEndDate(endDateStr);
        RequestValueUtils.validateDateRange(beginDate, endDate);

        Map<String, Object> contents = logService.getLogList(normalizedCurrPage, normalizedPageSize, beginDate, endDate);
        logService.saveAuditLog(ip, LogActionEnum.GET_LOG_LIST, Util.STATUS_SUCCESS, new Date(), null, description, user);
        return Util.suc(contents);
    }
}

package cn.ac.sitp.infrared.controller;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
import cn.ac.sitp.infrared.service.AuditLogService;
import cn.ac.sitp.infrared.service.JobService;
import cn.ac.sitp.infrared.util.Util;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping(value = "/rest/job")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    @Resource
    private HttpServletRequest request;

    @Autowired
    private AuditLogService logService;

    @Autowired
    private JobService jobService;

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public Map<String, Object> getJobList(@RequestBody JSONObject obj) {
        String ip = Util.getUserIpAddr(request);
//        Subject subject = SecurityUtils.getSubject();
//        AxrrAccount user = (AxrrAccount) subject.getPrincipal();
        AxrrAccount user = new AxrrAccount();
        user.setUserno(1);
        user.setDisplayname("管理员");
        user.setUserid("admin");
        user.setUsername("admin");

        int currPage = obj.getInteger("curr_page") == null ? 1 : obj.getInteger("curr_page");
        int pageSize = obj.getInteger("page_size") == null ? 10 : obj.getInteger("page_size");
        String description = LogActionEnum.GET_JOB_LIST.getDescription() + " " + obj;
        try {
            Map<String, Object> contents = jobService.getJobList(currPage, pageSize, user);
            logService.saveAuditLogDesc(ip, LogActionEnum.GET_JOB_LIST, Util.STATUS_SUCCESS, new Date(),
                    null, null, null, null, null, null, description, user);
            return Util.suc(contents);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            // msg就是最后取出来的字符串,可存数据库
            String msg = sw.toString();
            log.error("系统异常：", e);
            try {
                logService.saveAuditLogDesc(ip, LogActionEnum.GET_JOB_LIST, Util.STATUS_FAILURE, new Date(),
                        msg, null, null, null, null, null, description, user);
            } catch (Exception ex) {
                log.error("系统异常：", ex);
            }
            return Util.err(null, msg);
        }
    }

    @RequestMapping(value = "/algorithm/list", method = RequestMethod.POST)
    public Map<String, Object> getAlgorithmList(@RequestBody JSONObject obj) {
        String ip = Util.getUserIpAddr(request);
//        Subject subject = SecurityUtils.getSubject();
//        AxrrAccount user = (AxrrAccount) subject.getPrincipal();
        AxrrAccount user = new AxrrAccount();
        user.setUserno(1);
        user.setDisplayname("管理员");
        user.setUserid("admin");
        user.setUsername("admin");

        int currPage = obj.getInteger("curr_page") == null ? 1 : obj.getInteger("curr_page");
        int pageSize = obj.getInteger("page_size") == null ? 10 : obj.getInteger("page_size");
        String description = LogActionEnum.GET_ALGORITHM_LIST.getDescription() + " " + obj;
        try {
            Map<String, Object> contents = jobService.getAlgorithmList(currPage, pageSize);
            logService.saveAuditLogDesc(ip, LogActionEnum.GET_ALGORITHM_LIST, Util.STATUS_SUCCESS, new Date(),
                    null, null, null, null, null, null, description, user);
            return Util.suc(contents);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            // msg就是最后取出来的字符串,可存数据库
            String msg = sw.toString();
            log.error("系统异常：", e);
            try {
                logService.saveAuditLogDesc(ip, LogActionEnum.GET_ALGORITHM_LIST, Util.STATUS_FAILURE, new Date(),
                        msg, null, null, null, null, null, description, user);
            } catch (Exception ex) {
                log.error("系统异常：", ex);
            }
            return Util.err(null, msg);
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Map<String, Object> addJob(@RequestBody JSONObject obj) {
        String ip = Util.getUserIpAddr(request);
//        Subject subject = SecurityUtils.getSubject();
//        AxrrAccount user = (AxrrAccount) subject.getPrincipal();
        AxrrAccount user = new AxrrAccount();
        user.setUserno(1);
        user.setDisplayname("管理员");
        user.setUserid("admin");
        user.setUsername("admin");

        Long algorithmId = obj.getLong("algorithm_id");
        Long dataSetId = obj.getLong("data_set_id");
        String description = LogActionEnum.ADD_JOB.getDescription() + " " + obj;
        try {
            jobService.addJob(dataSetId, algorithmId, user);
            logService.saveAuditLogDesc(ip, LogActionEnum.ADD_JOB, Util.STATUS_SUCCESS, new Date(),
                    null, null, null, null, null, null, description, user);
            return Util.suc(null);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            // msg就是最后取出来的字符串,可存数据库
            String msg = sw.toString();
            log.error("系统异常：", e);
            try {
                logService.saveAuditLogDesc(ip, LogActionEnum.ADD_JOB, Util.STATUS_FAILURE, new Date(),
                        msg, null, null, null, null, null, description, user);
            } catch (Exception ex) {
                log.error("系统异常：", ex);
            }
            return Util.err(null, msg);
        }
    }

}

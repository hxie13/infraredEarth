package cn.ac.sitp.infrared.controller;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
import cn.ac.sitp.infrared.service.AuditLogService;
import cn.ac.sitp.infrared.service.NCService;
import cn.ac.sitp.infrared.util.Util;
import com.alibaba.fastjson2.JSONObject;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/rest/nc")
public class NCController {

    private static final Logger log = LoggerFactory.getLogger(NCController.class);

    @Resource
    private HttpServletRequest request;

    @Autowired
    private AuditLogService logService;

    @Autowired
    private NCService ncService;

    @RequestMapping(value = "/getType", method = RequestMethod.POST)
    public Map<String, Object> getDisasterTypeList(@RequestBody JSONObject obj) {
        String ip = Util.getUserIpAddr(request);
//        Subject subject = SecurityUtils.getSubject();
//        AxrrAccount user = (AxrrAccount) subject.getPrincipal();
        AxrrAccount user = new AxrrAccount();
        user.setUserno(1);
        user.setDisplayname("管理员");
        user.setUserid("admin");
        user.setUsername("admin");

        String description = LogActionEnum.GET_NC_TYPE_LIST.getDescription() + " " + obj;
        try {
            Map<String, Object> contents = ncService.getNCTypeList();
            logService.saveAuditLogDesc(ip, LogActionEnum.GET_NC_TYPE_LIST, Util.STATUS_SUCCESS, new Date(),
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
                logService.saveAuditLogDesc(ip, LogActionEnum.GET_NC_TYPE_LIST, Util.STATUS_FAILURE, new Date(),
                        msg, null, null, null, null, null, description, user);
            } catch (Exception ex) {
                log.error("系统异常：", ex);
            }
            return Util.err(null, msg);
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public Map<String, Object> getNCList(@RequestBody JSONObject obj) {
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
        String beginDateStr = obj.getString("begin_date");
        String endDateStr = obj.getString("end_date");
        String name = obj.getString("name");
        String title = obj.getString("title");
        Integer bandNumber = obj.getInteger("band_number");
        String regionName = obj.getString("region_name");
        String satelliteType = obj.getString("satellite_type");
        String resolution = obj.getString("resolution");
        String imgType = obj.getString("img_type");
        String processType = obj.getString("process_type");
        Date beginDate = null, endDate = null;
        if (!StringUtils.isEmpty(beginDateStr)) {
            beginDate = Util.strToDate(beginDateStr + " 00:00:00", Util.FORMAT_LONG);
        }
        if (!StringUtils.isEmpty(endDateStr)) {
            endDate = Util.strToDate(endDateStr + " 23:59:59", Util.FORMAT_LONG);
        }
        String description = LogActionEnum.GET_NC_LIST.getDescription() + " " + obj;
        try {
            Map<String, Object> contents = ncService.getNCList(currPage, pageSize, name, title, bandNumber, regionName,
                    satelliteType, resolution, imgType, processType, beginDate, endDate);
            logService.saveAuditLogDesc(ip, LogActionEnum.GET_NC_LIST, Util.STATUS_SUCCESS, new Date(),
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
                logService.saveAuditLogDesc(ip, LogActionEnum.GET_NC_LIST, Util.STATUS_FAILURE, new Date(),
                        msg, null, null, null, null, null, description, user);
            } catch (Exception ex) {
                log.error("系统异常：", ex);
            }
            return Util.err(null, msg);
        }
    }

    @GetMapping(value = "/file/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> getNcFile(@PathVariable Long id) {
        String ip = Util.getUserIpAddr(request);
//        Subject subject = SecurityUtils.getSubject();
//        AxrrAccount user = (AxrrAccount) subject.getPrincipal();
        AxrrAccount user = new AxrrAccount();
        user.setUserno(1);
        user.setDisplayname("管理员");
        user.setUserid("admin");
        user.setUsername("admin");

        String description = LogActionEnum.GET_NC_IMG.getDescription() + ", id:  " + id;
        try {
            InputStream inputStream = ncService.getNcFileStream(id);
            StreamingResponseBody responseBody = outputStream -> {
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    outputStream.write(data, 0, nRead);
                }
                inputStream.close();
            };
            logService.saveAuditLogDesc(ip, LogActionEnum.GET_NC_IMG, Util.STATUS_SUCCESS, new Date(),
                    null, null, null, null, null, null, description, user);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"nc_file_" + id + "\"")
                    .body(responseBody);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            // msg就是最后取出来的字符串,可存数据库
            String msg = sw.toString();
            log.error("系统异常：", e);
            try {
                logService.saveAuditLogDesc(ip, LogActionEnum.GET_NC_IMG, Util.STATUS_FAILURE, new Date(),
                        msg, null, null, null, null, null, description, user);
            } catch (Exception ex) {
                log.error("系统异常：", ex);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Map<String, Object> addDataSet(@RequestBody JSONObject obj) {
        String ip = Util.getUserIpAddr(request);
//        Subject subject = SecurityUtils.getSubject();
//        AxrrAccount user = (AxrrAccount) subject.getPrincipal();
        AxrrAccount user = new AxrrAccount();
        user.setUserno(1);
        user.setDisplayname("管理员");
        user.setUserid("admin");
        user.setUsername("admin");

        String ncIds = obj.getString("nc_ids");
        List<Long> ncIdList = Arrays.stream(ncIds.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .toList();
        String description = LogActionEnum.ADD_DATASET.getDescription() + " " + obj;
        try {
            ncService.addDataset(ncIdList, user);
            logService.saveAuditLogDesc(ip, LogActionEnum.ADD_DATASET, Util.STATUS_SUCCESS, new Date(),
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
                logService.saveAuditLogDesc(ip, LogActionEnum.ADD_DATASET, Util.STATUS_FAILURE, new Date(),
                        msg, null, null, null, null, null, description, user);
            } catch (Exception ex) {
                log.error("系统异常：", ex);
            }
            return Util.err(null, msg);
        }
    }
}

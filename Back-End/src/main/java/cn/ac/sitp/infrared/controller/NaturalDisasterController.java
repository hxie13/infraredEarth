package cn.ac.sitp.infrared.controller;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
import cn.ac.sitp.infrared.service.AuditLogService;
import cn.ac.sitp.infrared.service.NaturalDisasterService;
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
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(value = "/rest/naturaldisaster")
public class NaturalDisasterController {

    private static final Logger log = LoggerFactory.getLogger(NaturalDisasterController.class);

    @Resource
    private HttpServletRequest request;

    @Autowired
    private AuditLogService logService;

    @Autowired
    private NaturalDisasterService naturalDisasterService;

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

        String description = LogActionEnum.GET_NATURAL_DISASTER_TYPE_LIST.getDescription() + " " + obj;
        try {
            Map<String, Object> contents = naturalDisasterService.getNaturalDisasterTypeList();
            logService.saveAuditLogDesc(ip, LogActionEnum.GET_NATURAL_DISASTER_TYPE_LIST, Util.STATUS_SUCCESS, new Date(),
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
                logService.saveAuditLogDesc(ip, LogActionEnum.GET_NATURAL_DISASTER_TYPE_LIST, Util.STATUS_FAILURE, new Date(),
                        msg, null, null, null, null, null, description, user);
            } catch (Exception ex) {
                log.error("系统异常：", ex);
            }
            return Util.err(null, msg);
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public Map<String, Object> getNaturalDisasterList(@RequestBody JSONObject obj) {
        String ip = Util.getUserIpAddr(request);
//        Subject subject = SecurityUtils.getSubject();
//        AxrrAccount user = (AxrrAccount) subject.getPrincipal();
        AxrrAccount user = new AxrrAccount();
        user.setUserno(1);
        user.setDisplayname("管理员");
        user.setUserid("admin");
        user.setUsername("admin");

        String beginDateStr = obj.getString("begin_date");
        String endDateStr = obj.getString("end_date");
        Date beginDate = null, endDate = null;
        if (!StringUtils.isEmpty(beginDateStr)) {
            beginDate = Util.strToDate(beginDateStr + " 00:00:00", Util.FORMAT_LONG);
        }
        if (!StringUtils.isEmpty(endDateStr)) {
            endDate = Util.strToDate(endDateStr + " 23:59:59", Util.FORMAT_LONG);
        }
        String country = obj.getString("country");
        String place = obj.getString("place");
        String type = obj.getString("type");
        int currPage = obj.getInteger("curr_page") == null ? 1 : obj.getInteger("curr_page");
        int pageSize = obj.getInteger("page_size") == null ? 10 : obj.getInteger("page_size");
        String description = LogActionEnum.GET_NATURAL_DISASTER_LIST.getDescription() + " " + obj;
        try {
            Map<String, Object> contents = naturalDisasterService.getNaturalDisasterList(currPage, pageSize, beginDate, endDate, country, place, type);
            logService.saveAuditLogDesc(ip, LogActionEnum.GET_NATURAL_DISASTER_LIST, Util.STATUS_SUCCESS, new Date(),
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
                logService.saveAuditLogDesc(ip, LogActionEnum.GET_NATURAL_DISASTER_LIST, Util.STATUS_FAILURE, new Date(),
                        msg, null, null, null, null, null, description, user);
            } catch (Exception ex) {
                log.error("系统异常：", ex);
            }
            return Util.err(null, msg);
        }
    }

    @GetMapping(value = "/file/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> getNaturalDisasterFile(@PathVariable Long id,
                                                      @RequestParam(value = "img_type", required = false) Integer imgType) {
        String ip = Util.getUserIpAddr(request);
//        Subject subject = SecurityUtils.getSubject();
//        AxrrAccount user = (AxrrAccount) subject.getPrincipal();
        AxrrAccount user = new AxrrAccount();
        user.setUserno(1);
        user.setDisplayname("管理员");
        user.setUserid("admin");
        user.setUsername("admin");

        String description = LogActionEnum.GET_NATURAL_DISASTER_IMG.getDescription() + ", id:  " + id + ", img_type: " + imgType;
        try {
            Map<String, Object> contents = naturalDisasterService.getNaturalDisasterFileStream(id, imgType);
            List<Img> imgList = (List<Img>) contents.get("imgList");
            if (imgList == null || imgList.isEmpty()) {
                throw new FileNotFoundException("Natural disaster files not found for ID: " + id);
            }
            
            // 获取第一个图片的路径
            String imgPath = imgList.get(0).getImgPath();
            if (imgPath == null || imgPath.isEmpty()) {
                throw new FileNotFoundException("Image path not found for ID: " + id);
            }
            
            // 处理图片路径，将数据库中的绝对路径转换为相对路径
            String processedPath = imgPath;
            if (processedPath.startsWith("/data/")) {
                processedPath = processedPath.substring(6); // 移除"/data/"
            }
            
            // 构建正确的本地文件路径
            String basePath = System.getProperty("user.dir");
            String fullPath = basePath + "/" + processedPath;
            
            // 检查文件是否存在
            java.io.File file = new java.io.File(fullPath);
            if (!file.exists()) {
                // 尝试其他可能的路径格式
                fullPath = "/data/" + processedPath;
                file = new java.io.File(fullPath);
                if (!file.exists()) {
                    throw new FileNotFoundException("Natural disaster file not found at paths: " + imgPath + ", " + fullPath);
                }
            }
            
            // 读取文件并返回流
            InputStream inputStream = new FileInputStream(file);
            StreamingResponseBody responseBody = outputStream -> {
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    outputStream.write(data, 0, nRead);
                }
                inputStream.close();
            };
            
            logService.saveAuditLogDesc(ip, LogActionEnum.GET_NATURAL_DISASTER_IMG, Util.STATUS_SUCCESS, new Date(),
                    null, null, null, null, null, null, description, user);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"disaster_file_" + id + "\"")
                    .body(responseBody);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            // msg就是最后取出来的字符串,可存数据库
            String msg = sw.toString();
            log.error("系统异常：", e);
            try {
                logService.saveAuditLogDesc(ip, LogActionEnum.GET_NATURAL_DISASTER_IMG, Util.STATUS_FAILURE, new Date(),
                        msg, null, null, null, null, null, description, user);
            } catch (Exception ex) {
                log.error("系统异常：", ex);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

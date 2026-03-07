package cn.ac.sitp.infrared.controller;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
import cn.ac.sitp.infrared.security.SessionAccountHelper;
import cn.ac.sitp.infrared.service.AuditLogService;
import cn.ac.sitp.infrared.service.NaturalDisasterService;
import cn.ac.sitp.infrared.util.RequestValueUtils;
import cn.ac.sitp.infrared.util.Util;
import cn.ac.sitp.infrared.web.GlobalExceptionHandler;
import com.alibaba.fastjson2.JSONObject;
import cn.ac.sitp.infrared.web.request.NaturalDisasterListRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping(value = "/rest/naturaldisaster")
public class NaturalDisasterController {

    @Resource
    private HttpServletRequest request;

    @Autowired
    private AuditLogService logService;

    @Autowired
    private NaturalDisasterService naturalDisasterService;

    @RequestMapping(value = "/getType", method = RequestMethod.POST)
    public Map<String, Object> getDisasterTypeList(@RequestBody JSONObject obj) {
        String ip = Util.getUserIpAddr(request);
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        String description = LogActionEnum.GET_NATURAL_DISASTER_TYPE_LIST.getDescription() + " " + obj;
        GlobalExceptionHandler.setAuditContext(request, LogActionEnum.GET_NATURAL_DISASTER_TYPE_LIST, description);

        Map<String, Object> contents = naturalDisasterService.getNaturalDisasterTypeList();
        logService.saveAuditLog(ip, LogActionEnum.GET_NATURAL_DISASTER_TYPE_LIST, Util.STATUS_SUCCESS, new Date(), null, description, user);
        return Util.suc(contents);
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public Map<String, Object> getNaturalDisasterList(@RequestBody(required = false) NaturalDisasterListRequest requestBody) {
        String ip = Util.getUserIpAddr(request);
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        NaturalDisasterListRequest naturalDisasterListRequest =
                requestBody == null ? new NaturalDisasterListRequest() : requestBody;
        String description = LogActionEnum.GET_NATURAL_DISASTER_LIST.getDescription() + " " + naturalDisasterListRequest;
        GlobalExceptionHandler.setAuditContext(request, LogActionEnum.GET_NATURAL_DISASTER_LIST, description);

        int currPage = RequestValueUtils.normalizePage(naturalDisasterListRequest.getCurrPage());
        int pageSize = RequestValueUtils.normalizePageSize(naturalDisasterListRequest.getPageSize());
        Date beginDate = RequestValueUtils.parseBeginDate(naturalDisasterListRequest.getBeginDate());
        Date endDate = RequestValueUtils.parseEndDate(naturalDisasterListRequest.getEndDate());
        RequestValueUtils.validateDateRange(beginDate, endDate);

        Map<String, Object> contents = naturalDisasterService.getNaturalDisasterList(
                currPage, pageSize, beginDate, endDate,
                RequestValueUtils.trimToNull(naturalDisasterListRequest.getCountry()),
                RequestValueUtils.trimToNull(naturalDisasterListRequest.getPlace()),
                RequestValueUtils.trimToNull(naturalDisasterListRequest.getType()));
        logService.saveAuditLog(ip, LogActionEnum.GET_NATURAL_DISASTER_LIST, Util.STATUS_SUCCESS, new Date(), null, description, user);
        return Util.suc(contents);
    }

    @GetMapping(value = "/file/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getNaturalDisasterFile(
            @PathVariable Long id,
            @RequestParam(value = "img_type", required = false) Integer imgType) throws Exception {
        String ip = Util.getUserIpAddr(request);
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        String description = LogActionEnum.GET_NATURAL_DISASTER_IMG.getDescription()
                + ", id:  " + id + ", img_type: " + imgType;
        GlobalExceptionHandler.setAuditContext(request, LogActionEnum.GET_NATURAL_DISASTER_IMG, description);

        Long naturalDisasterId = RequestValueUtils.requirePositiveId(id);
        Integer normalizedImgType = normalizeImgType(imgType);
        Map<String, Object> contents = naturalDisasterService.getNaturalDisasterFileData(naturalDisasterId, normalizedImgType);

        logService.saveAuditLog(ip, LogActionEnum.GET_NATURAL_DISASTER_IMG, Util.STATUS_SUCCESS, new Date(), null, description, user);
        return Util.suc(contents);
    }

    private Integer normalizeImgType(Integer imgType) {
        if (imgType == null) {
            return null;
        }
        if (imgType < 0) {
            throw new IllegalArgumentException(RequestValueUtils.ILLEGAL_PARAMS_MESSAGE);
        }
        return imgType;
    }
}

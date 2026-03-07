package cn.ac.sitp.infrared.controller;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
import cn.ac.sitp.infrared.security.SessionAccountHelper;
import cn.ac.sitp.infrared.service.AuditLogService;
import cn.ac.sitp.infrared.service.NCService;
import cn.ac.sitp.infrared.util.RequestValueUtils;
import cn.ac.sitp.infrared.util.Util;
import cn.ac.sitp.infrared.web.GlobalExceptionHandler;
import com.alibaba.fastjson2.JSONObject;
import cn.ac.sitp.infrared.web.request.AddDatasetRequest;
import cn.ac.sitp.infrared.web.request.NcListRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/rest/nc")
public class NCController {

    @Resource
    private HttpServletRequest request;

    @Autowired
    private AuditLogService logService;

    @Autowired
    private NCService ncService;

    @RequestMapping(value = "/getType", method = RequestMethod.POST)
    public Map<String, Object> getDisasterTypeList(@RequestBody JSONObject obj) {
        String ip = Util.getUserIpAddr(request);
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        String description = LogActionEnum.GET_NC_TYPE_LIST.getDescription() + " " + obj;
        GlobalExceptionHandler.setAuditContext(request, LogActionEnum.GET_NC_TYPE_LIST, description);

        Map<String, Object> contents = ncService.getNCTypeList();
        logService.saveAuditLog(ip, LogActionEnum.GET_NC_TYPE_LIST, Util.STATUS_SUCCESS, new Date(), null, description, user);
        return Util.suc(contents);
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public Map<String, Object> getNCList(@RequestBody(required = false) NcListRequest requestBody) {
        String ip = Util.getUserIpAddr(request);
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        NcListRequest ncListRequest = requestBody == null ? new NcListRequest() : requestBody;
        String description = LogActionEnum.GET_NC_LIST.getDescription() + " " + ncListRequest;
        GlobalExceptionHandler.setAuditContext(request, LogActionEnum.GET_NC_LIST, description);

        int currPage = RequestValueUtils.normalizePage(ncListRequest.getCurrPage());
        int pageSize = RequestValueUtils.normalizePageSize(ncListRequest.getPageSize());
        Date beginDate = RequestValueUtils.parseBeginDate(ncListRequest.getBeginDate());
        Date endDate = RequestValueUtils.parseEndDate(ncListRequest.getEndDate());
        RequestValueUtils.validateDateRange(beginDate, endDate);

        Map<String, Object> contents = ncService.getNCList(currPage, pageSize,
                RequestValueUtils.trimToNull(ncListRequest.getName()),
                RequestValueUtils.trimToNull(ncListRequest.getTitle()),
                ncListRequest.getBandNumber(),
                RequestValueUtils.trimToNull(ncListRequest.getRegionName()),
                RequestValueUtils.trimToNull(ncListRequest.getSatelliteType()),
                RequestValueUtils.trimToNull(ncListRequest.getResolution()),
                RequestValueUtils.trimToNull(ncListRequest.getImgType()),
                RequestValueUtils.trimToNull(ncListRequest.getProcessType()),
                beginDate, endDate);
        logService.saveAuditLog(ip, LogActionEnum.GET_NC_LIST, Util.STATUS_SUCCESS, new Date(), null, description, user);
        return Util.suc(contents);
    }

    @GetMapping(value = "/file/{id}")
    public ResponseEntity<StreamingResponseBody> getNcFile(@PathVariable Long id) throws Exception {
        String ip = Util.getUserIpAddr(request);
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        String description = LogActionEnum.GET_NC_IMG.getDescription() + ", id:  " + id;
        GlobalExceptionHandler.setAuditContext(request, LogActionEnum.GET_NC_IMG, description);

        Path filePath = ncService.getNcFilePath(RequestValueUtils.requirePositiveId(id));
        InputStream inputStream = Files.newInputStream(filePath);
        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream stream = inputStream) {
                int nRead;
                byte[] data = new byte[8192];
                while ((nRead = stream.read(data, 0, data.length)) != -1) {
                    outputStream.write(data, 0, nRead);
                }
            }
        };
        logService.saveAuditLog(ip, LogActionEnum.GET_NC_IMG, Util.STATUS_SUCCESS, new Date(), null, description, user);
        String filename = filePath.getFileName() == null ? "nc_file_" + id : filePath.getFileName().toString();
        return ResponseEntity.ok()
                .contentType(resolveMediaType(filePath))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentLength(Files.size(filePath))
                .body(responseBody);
    }

    private MediaType resolveMediaType(Path filePath) {
        try {
            String contentType = Files.probeContentType(filePath);
            if (contentType != null) {
                return MediaType.parseMediaType(contentType);
            }
        } catch (IOException ignored) {
        }
        return MediaTypeFactory.getMediaType(filePath.getFileName().toString())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Map<String, Object> addDataSet(@RequestBody(required = false) AddDatasetRequest requestBody) {
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        String ip = Util.getUserIpAddr(request);
        AddDatasetRequest addDatasetRequest = requestBody == null ? new AddDatasetRequest() : requestBody;
        String description = LogActionEnum.ADD_DATASET.getDescription() + " " + addDatasetRequest;
        GlobalExceptionHandler.setAuditContext(request, LogActionEnum.ADD_DATASET, description);

        List<Long> ncIdList = RequestValueUtils.parsePositiveLongList(addDatasetRequest.getNcIds());
        ncService.addDataset(ncIdList, user);
        logService.saveAuditLog(ip, LogActionEnum.ADD_DATASET, Util.STATUS_SUCCESS, new Date(), null, description, user);
        return Util.suc(null);
    }
}

package cn.ac.sitp.infrared.controller;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
import cn.ac.sitp.infrared.security.SessionAccountHelper;
import cn.ac.sitp.infrared.service.AuditLogService;
import cn.ac.sitp.infrared.service.JobService;
import cn.ac.sitp.infrared.util.RequestValueUtils;
import cn.ac.sitp.infrared.util.Util;
import cn.ac.sitp.infrared.web.GlobalExceptionHandler;
import cn.ac.sitp.infrared.web.request.AddJobRequest;
import cn.ac.sitp.infrared.web.request.PaginationRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/rest/job")
@RequiredArgsConstructor
public class JobController {

    private final HttpServletRequest request;
    private final AuditLogService logService;
    private final JobService jobService;

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public Map<String, Object> getJobList(@RequestBody(required = false) PaginationRequest requestBody) {
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        String ip = Util.getUserIpAddr(request);
        PaginationRequest paginationRequest = requestBody == null ? new PaginationRequest() : requestBody;
        int currPage = RequestValueUtils.normalizePage(paginationRequest.getCurrPage());
        int pageSize = RequestValueUtils.normalizePageSize(paginationRequest.getPageSize());
        String description = LogActionEnum.GET_JOB_LIST.getDescription() + " " + paginationRequest;
        GlobalExceptionHandler.setAuditContext(request, LogActionEnum.GET_JOB_LIST, description);

        Map<String, Object> contents = jobService.getJobList(currPage, pageSize, user);
        logService.saveAuditLog(ip, LogActionEnum.GET_JOB_LIST, Util.STATUS_SUCCESS, new Date(), null, description, user);
        return Util.suc(contents);
    }

    @RequestMapping(value = "/algorithm/list", method = RequestMethod.POST)
    public Map<String, Object> getAlgorithmList(@RequestBody(required = false) PaginationRequest requestBody) {
        String ip = Util.getUserIpAddr(request);
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        PaginationRequest paginationRequest = requestBody == null ? new PaginationRequest() : requestBody;
        int currPage = RequestValueUtils.normalizePage(paginationRequest.getCurrPage());
        int pageSize = RequestValueUtils.normalizePageSize(paginationRequest.getPageSize());
        String description = LogActionEnum.GET_ALGORITHM_LIST.getDescription() + " " + paginationRequest;
        GlobalExceptionHandler.setAuditContext(request, LogActionEnum.GET_ALGORITHM_LIST, description);

        Map<String, Object> contents = jobService.getAlgorithmList(currPage, pageSize);
        logService.saveAuditLog(ip, LogActionEnum.GET_ALGORITHM_LIST, Util.STATUS_SUCCESS, new Date(), null, description, user);
        return Util.suc(contents);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Map<String, Object> addJob(@RequestBody(required = false) AddJobRequest requestBody) {
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        String ip = Util.getUserIpAddr(request);
        AddJobRequest addJobRequest = requestBody == null ? new AddJobRequest() : requestBody;
        String description = LogActionEnum.ADD_JOB.getDescription() + " " + addJobRequest;
        GlobalExceptionHandler.setAuditContext(request, LogActionEnum.ADD_JOB, description);

        Long algorithmId = RequestValueUtils.requirePositiveId(addJobRequest.getAlgorithmId());
        Long dataSetId = RequestValueUtils.requirePositiveId(addJobRequest.getDataSetId());
        Long jobId = jobService.addJob(dataSetId, algorithmId, user);
        jobService.simulateJobExecution(jobId);
        logService.saveAuditLog(ip, LogActionEnum.ADD_JOB, Util.STATUS_SUCCESS, new Date(), null, description, user);
        Map<String, Object> contents = new HashMap<>();
        contents.put("jobId", jobId);
        return Util.suc(contents);
    }
}

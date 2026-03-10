package cn.ac.sitp.infrared.controller;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
import cn.ac.sitp.infrared.security.AccountAuthenticationException;
import cn.ac.sitp.infrared.security.SessionAccountHelper;
import cn.ac.sitp.infrared.service.AccountService;
import cn.ac.sitp.infrared.service.AuditLogService;
import cn.ac.sitp.infrared.util.AESLoginUtil;
import cn.ac.sitp.infrared.util.Util;
import cn.ac.sitp.infrared.web.request.LoginRequest;
import cn.ac.sitp.infrared.web.request.PasswordUpdateRequest;
import cn.ac.sitp.infrared.web.request.RegisterRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/rest/account")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    @Resource
    private HttpServletRequest request;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuditLogService logService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public Map<String, Object> account() {
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        if (user == null) {
            return Util.noLogin();
        }
        Map<String, Object> contents = new HashMap<>();
        contents.put("account", SessionAccountHelper.sanitize(user));
        return Util.suc(contents);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Map<String, Object> login(@RequestBody(required = false) LoginRequest requestBody) {
        String ip = Util.getUserIpAddr(request);
        LoginRequest loginRequest = requestBody == null ? new LoginRequest() : requestBody;
        String username = decodeCredential(loginRequest.getUsername());
        String password = decodeCredential(loginRequest.getPassword());

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return Util.err(null, "Illegal Params");
        }

        try {
            AxrrAccount user = accountService.loginAccount(username, password);
            SessionAccountHelper.storeAccount(request, user);
            AxrrAccount sessionUser = SessionAccountHelper.currentAccount(request);
            logService.saveAccountAuditLog(ip, LogActionEnum.LOGIN, Util.STATUS_SUCCESS, new Date(),
                    sessionUser.getUserid(), sessionUser.getDisplayname(), null);
            return account();
        } catch (AccountAuthenticationException e) {
            log.warn("Login failed for user {}", username, e);
            logService.saveAccountAuditLog(ip, LogActionEnum.LOGIN, Util.STATUS_FAILURE, new Date(), null, username, e.getMessage());
            return Util.err(null, e.getMessage());
        } catch (Exception e) {
            String stackTrace = Util.getStackTrace(e);
            log.error("Unexpected login error", e);
            logService.saveAccountAuditLog(ip, LogActionEnum.LOGIN, Util.STATUS_FAILURE, new Date(), null, username, stackTrace);
            return Util.err(null, Util.GENERIC_ERROR_MESSAGE);
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public Map<String, Object> logout() {
        String ip = Util.getUserIpAddr(request);
        AxrrAccount user = SessionAccountHelper.currentAccount(request);
        if (user == null) {
            return Util.noLogin();
        }
        try {
            logService.saveAccountAuditLog(ip, LogActionEnum.LOGOUT, Util.STATUS_SUCCESS, new Date(),
                    user.getUserid(), user.getDisplayname(), null);
        } finally {
            SessionAccountHelper.clearAccount(request);
        }
        return Util.suc(null);
    }

    @RequestMapping(value = "/password", method = RequestMethod.POST)
    public Map<String, Object> updatePassword(@RequestBody(required = false) PasswordUpdateRequest requestBody) {
        String ip = Util.getUserIpAddr(request);
        AxrrAccount currentUser = SessionAccountHelper.currentAccount(request);
        if (currentUser == null) {
            return Util.noLogin();
        }

        PasswordUpdateRequest passwordUpdateRequest = requestBody == null ? new PasswordUpdateRequest() : requestBody;
        String oldPassword = decodeCredential(passwordUpdateRequest.getOldpassword());
        String newPassword = decodeCredential(passwordUpdateRequest.getPassword());
        if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
            return Util.err(null, "Illegal Params");
        }

        try {
            accountService.updatePassword(currentUser.getUsername(), oldPassword, newPassword);
            AxrrAccount refreshedUser = accountService.loginAccount(currentUser.getUsername(), newPassword);
            SessionAccountHelper.storeAccount(request, refreshedUser);
            logService.saveAccountAuditLog(ip, LogActionEnum.UPDATE_PASSWORD, Util.STATUS_SUCCESS, new Date(),
                    refreshedUser.getUserid(), refreshedUser.getDisplayname(), null);
            return Util.suc(null);
        } catch (AccountAuthenticationException e) {
            log.warn("Password update failed for user {}", currentUser.getUsername(), e);
            logService.saveAccountAuditLog(ip, LogActionEnum.UPDATE_PASSWORD, Util.STATUS_FAILURE, new Date(),
                    currentUser.getUserid(), currentUser.getUsername(), e.getMessage());
            return Util.err(null, e.getMessage());
        } catch (Exception e) {
            String stackTrace = Util.getStackTrace(e);
            log.error("Unexpected password update error", e);
            logService.saveAccountAuditLog(ip, LogActionEnum.UPDATE_PASSWORD, Util.STATUS_FAILURE, new Date(),
                    currentUser.getUserid(), currentUser.getUsername(), stackTrace);
            return Util.err(null, Util.GENERIC_ERROR_MESSAGE);
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Map<String, Object> register(@RequestBody(required = false) RegisterRequest requestBody) {
        String ip = Util.getUserIpAddr(request);
        RegisterRequest registerRequest = requestBody == null ? new RegisterRequest() : requestBody;
        
        String username = decodeCredential(registerRequest.getUsername());
        String password = decodeCredential(registerRequest.getPassword());
        String displayname = decodeCredential(registerRequest.getDisplayname());
        String email = decodeCredential(registerRequest.getEmail());

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return Util.err(null, "Illegal Params");
        }

        try {
            AxrrAccount user = accountService.registerAccount(username, password, displayname, email);
            logService.saveAccountAuditLog(ip, LogActionEnum.REGISTER, Util.STATUS_SUCCESS, new Date(),
                    user.getUserid(), user.getDisplayname(), null);
            
            // Auto-login after registration
            SessionAccountHelper.storeAccount(request, user);
            Map<String, Object> contents = new HashMap<>();
            contents.put("account", SessionAccountHelper.sanitize(user));
            return Util.suc(contents);
        } catch (AccountAuthenticationException e) {
            log.warn("Registration failed for user {}", username, e);
            logService.saveAccountAuditLog(ip, LogActionEnum.REGISTER, Util.STATUS_FAILURE, new Date(), null, username, e.getMessage());
            return Util.err(null, e.getMessage());
        } catch (Exception e) {
            String stackTrace = Util.getStackTrace(e);
            log.error("Unexpected registration error", e);
            logService.saveAccountAuditLog(ip, LogActionEnum.REGISTER, Util.STATUS_FAILURE, new Date(), null, username, stackTrace);
            return Util.err(null, Util.GENERIC_ERROR_MESSAGE);
        }
    }

    private String decodeCredential(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        try {
            return AESLoginUtil.aesDecryptString(value, null);
        } catch (Exception e) {
            log.warn("Failed to decrypt credential, rejecting request", e);
            throw new AccountAuthenticationException("Invalid credentials format");
        }
    }
}

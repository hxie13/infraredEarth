//package cn.ac.sitp.infrared.controller;
//
//import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
//import cn.ac.sitp.infrared.datasource.enumeration.LogActionEnum;
//import cn.ac.sitp.infrared.service.AccountService;
//import cn.ac.sitp.infrared.service.AuditLogService;
//import cn.ac.sitp.infrared.util.AESLoginUtil;
//import cn.ac.sitp.infrared.util.Util;
//import com.alibaba.fastjson2.JSONObject;
//import jakarta.annotation.Resource;
//import jakarta.servlet.http.HttpServletRequest;
//import org.apache.commons.codec.digest.DigestUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.shiro.SecurityUtils;
//import org.apache.shiro.authc.UsernamePasswordToken;
//import org.apache.shiro.subject.Subject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping(value = "/rest/account")
//public class AccountController {
//
//    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
//
//    @Resource
//    private HttpServletRequest request;
//
//    @Autowired
//    private AccountService accountService;
//
//    @Autowired
//    private AuditLogService logService;
//
//    @RequestMapping(value = "/login", method = RequestMethod.GET)
//    public Map<String, Object> account() {
//        Map<String, Object> contents = new HashMap<>();
//        Subject subject = SecurityUtils.getSubject();
//        if (subject == null || !subject.isAuthenticated()) {
//            return Util.noLogin();
//        }
//        AxrrAccount user = (AxrrAccount) subject.getPrincipal();
//        contents.put("account", user);
//        return Util.suc(contents);
//    }
//
//    @RequestMapping(value = "/login", method = RequestMethod.POST)
//    public Map<String, Object> login(@RequestBody JSONObject obj) {
//        String ip = Util.getUserIpAddr(request);
//        String username, password;
//        try {
//            username = obj.getString("username");
//            password = obj.getString("password");
//            username = AESLoginUtil.aesDecryptString(username, AESLoginUtil.KEY);
//            password = AESLoginUtil.aesDecryptString(password, AESLoginUtil.KEY);
////            if (password.equals(username + "123")) return Util.err(null, "请修改密码后再重新登录！");
//            if (!StringUtils.trimToEmpty(password).isEmpty()) {
//                password = DigestUtils.md5Hex(password);
//            }
//        } catch (Exception e) {
//            log.error("系统异常：", e);
//            return Util.err(null, "Illegal Params");
//        }
//
//        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
//        token.setHost(Util.getUserIpAddr(request));
//        Subject currentUser = SecurityUtils.getSubject();
//        try {
//            currentUser.login(token);
//            Subject subject = SecurityUtils.getSubject();
//            AxrrAccount user = (AxrrAccount) subject.getPrincipal();
//            String userName = user.getDisplayname();
//            logService.saveAccountAuditLog(ip, LogActionEnum.LOGIN, Util.STATUS_SUCCESS, new Date(), user.getUserid(), userName, null);
//        } catch (Exception e) {
//            log.error("系统异常：", e);
//            StringWriter sw = new StringWriter();
//            PrintWriter pw = new PrintWriter(sw);
//            e.printStackTrace(pw);
//            // msg就是最后取出来的字符串,可存数据库
//            String msg = sw.toString();
//            try {
//                logService.saveAccountAuditLog(ip, LogActionEnum.LOGIN, Util.STATUS_SUCCESS, new Date(), null, username, msg);
//            } catch (Exception ex) {
//                log.error("系统异常：", ex);
//            }
//            return Util.err(null, msg);
//        }
//        return account();
//    }
//
//    @RequestMapping(value = "/logout", method = RequestMethod.GET)
//    public Map<String, Object> logout() {
//        String ip = Util.getUserIpAddr(request);
//        Subject currentUser = SecurityUtils.getSubject();
//        AxrrAccount user = (AxrrAccount) currentUser.getPrincipal();
//        String username = user.getDisplayname();
//        if (currentUser.isAuthenticated()) {
//            try {
//                logService.saveAccountAuditLog(ip, LogActionEnum.LOGOUT, Util.STATUS_SUCCESS, new Date(), user.getUserid(), username, null);
//                currentUser.logout();
//            } catch (Exception e) {
//                log.error("系统异常：", e);
//            }
//        } else {
//            return Util.noLogin();
//        }
//        return Util.suc(null);
//    }
//
//    @RequestMapping(value = "/password", method = RequestMethod.POST)
//    public Map<String, Object> updatePassword(@RequestBody JSONObject obj) {
//        String ip = Util.getUserIpAddr(request);
//        String username, oldPassword, password;
//        try {
//            username = obj.getString("username");
//            oldPassword = obj.getString("oldpassword");
//            password = obj.getString("password");
//            username = AESLoginUtil.aesDecryptString(username, AESLoginUtil.KEY);
//            oldPassword = AESLoginUtil.aesDecryptString(oldPassword, AESLoginUtil.KEY);
//            password = AESLoginUtil.aesDecryptString(password, AESLoginUtil.KEY);
//            if (!StringUtils.trimToEmpty(password).isEmpty()) {
//                password = DigestUtils.md5Hex(password);
//            }
//            if (!StringUtils.trimToEmpty(oldPassword).isEmpty()) {
//                oldPassword = DigestUtils.md5Hex(oldPassword);
//            }
//        } catch (Exception e) {
//            log.error("系统异常：", e);
//            return Util.err(null, "Illegal Params");
//        }
//
//        try {
//            AxrrAccount user = accountService.updatePassword(username, oldPassword, password);
//            logService.saveAccountAuditLog(ip, LogActionEnum.UPDATE_PASSWORD, Util.STATUS_SUCCESS, new Date(), user.getUserid(), username, null);
//            return Util.suc(null);
//        } catch (Exception e) {
//            log.error("系统异常：", e);
//            StringWriter sw = new StringWriter();
//            PrintWriter pw = new PrintWriter(sw);
//            e.printStackTrace(pw);
//            // msg就是最后取出来的字符串,可存数据库
//            String msg = sw.toString();
//            try {
//                logService.saveAccountAuditLog(ip, LogActionEnum.UPDATE_PASSWORD, Util.STATUS_SUCCESS, new Date(), null, username, null);
//            } catch (Exception ex) {
//                log.error("系统异常：", ex);
//            }
//            return Util.err(null, msg);
//        }
//    }
//
//}

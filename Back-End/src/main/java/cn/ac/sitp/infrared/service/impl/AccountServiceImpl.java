package cn.ac.sitp.infrared.service.impl;

import cn.ac.sitp.infrared.datasource.dao.*;
import cn.ac.sitp.infrared.datasource.mapper.GlobalAccountMapper;
import cn.ac.sitp.infrared.service.AccountService;
import cn.ac.sitp.infrared.util.Util;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService {

    @Value("${app.login.expiredday}")
    long LOGIN_EXPIRED_DAYS;
    @Value("${app.login.maxfailurecount}")
    int MAX_LOGIN_ATTEMPTS;
    @Value("${app.login.lockminutes}")
    long LOGIN_LOCK_MINUTES;
    @Autowired
    private GlobalAccountMapper accountMapper;

    @Override
    public AxrrAccount loginAccount(String username, String password) throws AuthenticationException {
        ZoneId zoneId = ZoneId.systemDefault();
        AxrrAccount user = accountMapper.getUserByName(username);
        if (user == null) {
            throw new AuthenticationException("用户不存在或用户已禁用");
        }
        LocalDateTime expiredTime = LocalDateTime.ofInstant(user.getExpiration_time().toInstant(), zoneId);
        expiredTime = expiredTime.plusDays(LOGIN_EXPIRED_DAYS);
        if (expiredTime.isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("您的密码已过期，请前往修改");
        }
        if ("Y".equals(user.getLock_status())) {
            LocalDateTime unlockTime = LocalDateTime.ofInstant(user.getLock_time().toInstant(), zoneId);
            unlockTime = unlockTime.plusMinutes(LOGIN_LOCK_MINUTES);
            if (unlockTime.isBefore(LocalDateTime.now())) {
                accountMapper.lockAccount(user.getUserid(), "N");
                user.setFailure_count(0);
                user.setLock_status("N");
            } else {
                throw new AuthenticationException("用户账户已锁定，请于" + DateTimeFormatter.ofPattern(Util.FORMAT_LONG).format(unlockTime) + "后重试");
            }
        }
        if (user.getPassword().equals(password)) {
            accountMapper.resetFailureCount(user.getUserid());
        } else {
            accountMapper.increaseFailureCount(user.getUserid());
            int left = MAX_LOGIN_ATTEMPTS - user.getFailure_count() - 1;
            if (left <= 0) {
                accountMapper.lockAccount(user.getUserid(), "Y");
                LocalDateTime unlockTime = LocalDateTime.now();
                unlockTime = unlockTime.plusMinutes(LOGIN_LOCK_MINUTES);
                throw new AuthenticationException("用户账户已锁定，请于" + DateTimeFormatter.ofPattern(Util.FORMAT_LONG).format(unlockTime) + "后重试");
            } else {
                throw new AuthenticationException("密码错误，您还有" + left + "次机会重试");
            }
        }
        return user;
    }


    @Override
    public Map<String, Object> getRolePermission() throws Exception {
        Map<String, Object> contents = new HashMap<>();
        List<AxrrRole> rolelist = accountMapper.getRoleList();
        List<AxrrPermission> permissionlist = accountMapper.getPermissionList();
        List<AxrrRolePermission> role_permission_list = accountMapper.getRolePermissionList();
        contents.put("roles", rolelist);
        contents.put("permissions", permissionlist);
        contents.put("rolepermissions", role_permission_list);
        return contents;
    }

    @Override
    public AxrrAccount updatePassword(String username, String oldPassword, String password) throws Exception {
        AxrrAccount account = this.loginAccount(username, oldPassword);
        AxrrUser user = new AxrrUser();
        user.setUserid(account.getUserid());
        user.setPassword(password);
        accountMapper.updatePassword(user);
        return account;
    }
}

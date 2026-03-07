package cn.ac.sitp.infrared.service.impl;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.dao.AxrrPermission;
import cn.ac.sitp.infrared.datasource.dao.AxrrRole;
import cn.ac.sitp.infrared.datasource.dao.AxrrRolePermission;
import cn.ac.sitp.infrared.datasource.dao.AxrrUser;
import cn.ac.sitp.infrared.datasource.mapper.GlobalAccountMapper;
import cn.ac.sitp.infrared.security.AccountAuthenticationException;
import cn.ac.sitp.infrared.service.AccountService;
import cn.ac.sitp.infrared.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService {

    @Value("${app.login.expiredday}")
    private long loginExpiredDays;

    @Value("${app.login.maxfailurecount}")
    private int maxLoginAttempts;

    @Value("${app.login.lockminutes}")
    private long loginLockMinutes;

    @Autowired
    private GlobalAccountMapper accountMapper;

    @Autowired
    private PasswordService passwordService;

    @Override
    public AxrrAccount loginAccount(String username, String password) {
        ZoneId zoneId = ZoneId.systemDefault();
        AxrrAccount user = accountMapper.getUserByName(username);
        if (user == null) {
            throw new AccountAuthenticationException("Account does not exist or is disabled");
        }

        if (user.getExpiration_time() != null) {
            LocalDateTime expiredTime = LocalDateTime.ofInstant(user.getExpiration_time().toInstant(), zoneId)
                    .plusDays(loginExpiredDays);
            if (expiredTime.isBefore(LocalDateTime.now())) {
                throw new AccountAuthenticationException("Password has expired");
            }
        }

        if ("Y".equalsIgnoreCase(user.getLock_status()) && user.getLock_time() != null) {
            LocalDateTime unlockTime = LocalDateTime.ofInstant(user.getLock_time().toInstant(), zoneId)
                    .plusMinutes(loginLockMinutes);
            if (unlockTime.isBefore(LocalDateTime.now())) {
                accountMapper.lockAccount(user.getUserid(), "N");
                user.setFailure_count(0);
                user.setLock_status("N");
            } else {
                throw new AccountAuthenticationException("Account is locked until "
                        + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(unlockTime));
            }
        }

        // Check password with migration support
        boolean isLegacyMd5 = !passwordService.isBCryptHash(user.getPassword());
        if (passwordService.verifyPassword(password, user.getPassword(), isLegacyMd5)) {
            accountMapper.resetFailureCount(user.getUserid());
            // If using legacy MD5, upgrade to BCrypt
            if (isLegacyMd5) {
                upgradePasswordToBCrypt(user.getUserid(), password);
            }
        } else {
            accountMapper.increaseFailureCount(user.getUserid());
            int left = maxLoginAttempts - user.getFailure_count() - 1;
            if (left <= 0) {
                accountMapper.lockAccount(user.getUserid(), "Y");
                LocalDateTime unlockTime = LocalDateTime.now().plusMinutes(loginLockMinutes);
                throw new AccountAuthenticationException("Account is locked until "
                        + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(unlockTime));
            }
            throw new AccountAuthenticationException("Incorrect password, remaining attempts: " + left);
        }
        return user;
    }

    /**
     * Upgrade password hash from MD5 to BCrypt.
     */
    private void upgradePasswordToBCrypt(String userid, String rawPassword) {
        try {
            String newHash = passwordService.hashPassword(rawPassword);
            AxrrUser user = new AxrrUser();
            user.setUserid(userid);
            user.setPassword(newHash);
            accountMapper.updatePassword(user);
        } catch (Exception e) {
            // Log but don't fail login if upgrade fails
            // This ensures users can still login even if upgrade fails
        }
    }

    @Override
    public Map<String, Object> getRolePermission() {
        Map<String, Object> contents = new HashMap<>();
        List<AxrrRole> roleList = accountMapper.getRoleList();
        List<AxrrPermission> permissionList = accountMapper.getPermissionList();
        List<AxrrRolePermission> rolePermissionList = accountMapper.getRolePermissionList();
        contents.put("roles", roleList);
        contents.put("permissions", permissionList);
        contents.put("rolepermissions", rolePermissionList);
        return contents;
    }

    @Override
    @Transactional
    public AxrrAccount updatePassword(String username, String oldPassword, String newPassword) throws Exception {
        AxrrAccount account = loginAccount(username, oldPassword);
        
        // Hash new password with BCrypt
        String hashedPassword = passwordService.hashPassword(newPassword);
        
        AxrrUser user = new AxrrUser();
        user.setUserid(account.getUserid());
        user.setPassword(hashedPassword);
        accountMapper.updatePassword(user);
        return account;
    }
}

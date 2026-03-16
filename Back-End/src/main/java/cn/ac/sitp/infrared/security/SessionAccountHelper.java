package cn.ac.sitp.infrared.security;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionAccountHelper {

    public static final String SESSION_ACCOUNT_KEY = "infrared.currentAccount";

    private SessionAccountHelper() {
    }

    public static AxrrAccount currentAccount(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object account = session.getAttribute(SESSION_ACCOUNT_KEY);
        if (account instanceof AxrrAccount axrrAccount) {
            return axrrAccount;
        }
        return null;
    }

    public static void storeAccount(HttpServletRequest request, AxrrAccount account) {
        if (request == null || account == null) {
            return;
        }
        request.getSession(true).setAttribute(SESSION_ACCOUNT_KEY, sanitize(account));
    }

    public static void clearAccount(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_ACCOUNT_KEY);
            session.invalidate();
        }
    }

    public static AxrrAccount sanitize(AxrrAccount source) {
        if (source == null) {
            return null;
        }
        AxrrAccount target = new AxrrAccount();
        target.setUserno(source.getUserno());
        target.setDisplayname(source.getDisplayname());
        target.setPhone(source.getPhone());
        target.setEmail(source.getEmail());
        target.setUserid(source.getUserid());
        target.setUsername(source.getUsername());
        target.setType(source.getType());
        target.setSignature(source.getSignature());
        target.setClinicPermissions(source.getClinicPermissions());
        target.setFailureCount(source.getFailureCount());
        target.setUpdatetime(source.getUpdatetime());
        target.setExpirationTime(source.getExpirationTime());
        target.setLockTime(source.getLockTime());
        target.setLockStatus(source.getLockStatus());
        target.setValidTime(source.getValidTime());
        target.setPassword(null);
        return target;
    }
}

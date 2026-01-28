package cn.ac.sitp.infrared.service;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import org.apache.shiro.authc.AuthenticationException;

import java.util.Map;

public interface AccountService {

    AxrrAccount loginAccount(String username, String password) throws AuthenticationException;

    Map<String, Object> getRolePermission() throws Exception;

    AxrrAccount updatePassword(String username, String oldPassword, String password) throws Exception;

}

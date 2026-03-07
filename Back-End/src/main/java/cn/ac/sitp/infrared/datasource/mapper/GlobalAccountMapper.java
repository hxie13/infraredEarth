package cn.ac.sitp.infrared.datasource.mapper;

import cn.ac.sitp.infrared.datasource.dao.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GlobalAccountMapper {

    List<AxrrRole> getRoleList();

    List<AxrrPermission> getPermissionList();

    List<AxrrRolePermission> getRolePermissionList();

    AxrrAccount getUserByName(String username);

    void lockAccount(@Param("userid") String userid,
                     @Param("lock_status") String lock_status);

    void resetFailureCount(String userid);

    void increaseFailureCount(String userid);

    void updatePassword(AxrrUser user);

    int countUserByUsername(String username);

    int countUserByEmail(String email);

    void insertUser(AxrrUser user);
}

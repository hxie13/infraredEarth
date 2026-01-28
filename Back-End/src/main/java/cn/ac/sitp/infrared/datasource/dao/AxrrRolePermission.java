package cn.ac.sitp.infrared.datasource.dao;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class AxrrRolePermission implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 2588089263234928466L;

    private long fk_role_no;

    private long fk_permission_no;


}

package cn.ac.sitp.infrared.datasource.dao;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class AxrrAccount implements Serializable {

    @Serial
    private static final long serialVersionUID = 2588089263234928466L;

    private long userno;

    private String password;

    private String displayname;

    private String phone;

    private String email;

    private String userid;

    private String username;

    private String type;

    private String signature;

    private List<AxrrClinicPermission> clinicPermissions;

    private int failure_count;

    private Date updatetime;

    private Date expiration_time;

    private Date lock_time;

    private String lock_status;

    private Date valid_time;

}

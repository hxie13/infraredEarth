package cn.ac.sitp.infrared.datasource.dao;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;

@Setter
@Getter
public class AxrrUser implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 2588089263234928466L;

    private long userno;

    private Date creationtime;

    private String displayname;

    private String email;

    private String password;

    private Date updatetime;

    private String userid;

    private String username;

    private boolean notified;

    private boolean sentemail;

    private String type;

    private String signature;

    private long fk_role_no;


}

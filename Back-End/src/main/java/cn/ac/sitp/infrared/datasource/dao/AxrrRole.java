package cn.ac.sitp.infrared.datasource.dao;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;

@Setter
@Getter
public class AxrrRole implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 2588089263234928466L;

    private long role_no;

    private String role_id;

    private String role_name;

    private String role_desc;

    private Boolean valid;

    private Date creation_time;

    private String update_by;

    private Date update_time;


}

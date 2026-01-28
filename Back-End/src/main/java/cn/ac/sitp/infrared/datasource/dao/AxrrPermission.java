package cn.ac.sitp.infrared.datasource.dao;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;

@Setter
@Getter
public class AxrrPermission implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 2588089263234928466L;

    private long permission_no;
    private String permission_id;
    private String permission_name;
    private String permission_desc;
    private Boolean valid;
    private Date creation_time;
    private String fill;
    private Date update_time;


}

package cn.ac.sitp.infrared.datasource.dao;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

@Setter
@Getter
public class AxrrClinicPermission implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 2588089263234928466L;

    private long organization_no;

    private String organization_id;

    private String organization_name;

    private String description;

    private List<AxrrPermission> permissionlist;


}

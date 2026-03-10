package cn.ac.sitp.infrared.datasource.dao;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;

@Setter
@Getter
public class AxrrUser extends AxrrAccount {

    @Serial
    private static final long serialVersionUID = 2588089263234928466L;

    private Date creationtime;

    private boolean notified;

    private boolean sentemail;

    private long fk_role_no;
}

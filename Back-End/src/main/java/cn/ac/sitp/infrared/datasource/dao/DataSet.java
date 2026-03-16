package cn.ac.sitp.infrared.datasource.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
public class DataSet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long insertUserId;
    private Boolean delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date insertTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date updateTime;

}

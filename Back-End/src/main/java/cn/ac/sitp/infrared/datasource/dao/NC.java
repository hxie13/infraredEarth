package cn.ac.sitp.infrared.datasource.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
public class NC implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String ncName;
    private String title;
    private Integer bandNumber;
    private String ncPath;
    private String imgPath;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date endTime;

    private String ncGeometry;
    private China china;
    private String satelliteType;
    private String resolution;
    private String imgType;
    private String processType;

}

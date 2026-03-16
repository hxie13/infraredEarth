package cn.ac.sitp.infrared.datasource.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class Job implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Algorithm algorithm;
    private String jobStatus;
    private DataSet dataSet;
    private List<NC> ncList;
    private Long insertUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date insertTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date updateTime;

    private String resultPath;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date startedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date completedTime;

    private String parameters;

}

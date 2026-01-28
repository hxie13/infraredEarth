package cn.ac.sitp.infrared.datasource.dao;

import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import cn.ac.sitp.infrared.util.Util;

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
    private Date startTime;
    private Date endTime;
    private String ncGeometry;
    private China china;
    private String satelliteType;
    private String resolution;
    private String imgType;
    private String processType;
    private String startTimeStr;
    private String endTimeStr;

    public String getStartTimeStr() {
        return Util.dateToStringLong(this.startTime, Util.FORMAT_LONG);
    }

    public void setStartTimeStr(String startTimeStr) {
        this.startTimeStr = Util.dateToStringLong(this.startTime, Util.FORMAT_LONG);
    }

    public String getEndTimeStr() {
        return Util.dateToStringLong(this.endTime, Util.FORMAT_LONG);
    }

    public void setEndTimeStr(String endTimeStr) {
        this.endTimeStr = Util.dateToStringLong(this.endTime, Util.FORMAT_LONG);
    }

}

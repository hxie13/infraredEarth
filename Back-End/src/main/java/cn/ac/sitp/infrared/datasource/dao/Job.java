package cn.ac.sitp.infrared.datasource.dao;

import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import cn.ac.sitp.infrared.util.Util;

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
    private Date insertTime;
    private Date updateTime;
    private String insertTimeStr;
    private String updateTimeStr;

    public String getInsertTimeStr() {
        return Util.dateToStringLong(this.insertTime, Util.FORMAT_LONG);
    }

    public void setInsertTimeStr(String insertTimeStr) {
        this.insertTimeStr = Util.dateToStringLong(this.insertTime, Util.FORMAT_LONG);
    }

    public String getUpdateTimeStr() {
        return Util.dateToStringLong(this.updateTime, Util.FORMAT_LONG);
    }

    public void setUpdateTimeStr(String updateTimeStr) {
        this.updateTimeStr = Util.dateToStringLong(this.updateTime, Util.FORMAT_LONG);
    }

}

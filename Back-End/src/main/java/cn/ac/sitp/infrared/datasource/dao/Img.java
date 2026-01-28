package cn.ac.sitp.infrared.datasource.dao;

import cn.ac.sitp.infrared.util.Util;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
public class Img implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String imgPath;
    private Long ncId;
    private Long naturalDisasterId;
    private Date time;
    private String geometry;
    private Integer imgType;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String timeStr;

    public String getTimeStr() {
        return Util.dateToStringLong(this.time, Util.FORMAT_LONG);
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = Util.dateToStringLong(this.time, Util.FORMAT_LONG);
    }
}

package cn.ac.sitp.infrared.datasource.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date time;

    private String geometry;
    private Integer imgType;
    private BigDecimal longitude;
    private BigDecimal latitude;

    /** Backward-compatible formatted time string for frontend (examples.html relies on this) */
    public String getTimeStr() {
        if (time == null) return null;
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
    }

}

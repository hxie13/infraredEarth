package cn.ac.sitp.infrared.datasource.mapper;

import cn.ac.sitp.infrared.datasource.dao.AxrrAuditEvent;
import cn.ac.sitp.infrared.datasource.dao.Img;
import cn.ac.sitp.infrared.datasource.dao.NaturalDisaster;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface NaturalDisasterMapper {

    List<NaturalDisaster> selectNaturalDisasterList(@Param("offset") int offset,
                                                    @Param("page_size") int pageSize,
                                                    @Param("begin_date") Date beginDate,
                                                    @Param("end_date") Date endDate,
                                                    @Param("country") String country,
                                                    @Param("place") String place,
                                                    @Param("type") String type);

    int selectNaturalDisasterCount(@Param("begin_date") Date beginDate,
                                   @Param("end_date") Date endDate,
                                   @Param("country") String country,
                                   @Param("place") String place,
                                   @Param("type") String type);

    List<Img> getImgPathById(@Param("id") Long id,
                             @Param("img_type") Integer imgType);

    List<String> selectNaturalDisasterTypeList();
}

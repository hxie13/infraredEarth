package cn.ac.sitp.infrared.datasource.mapper;

import cn.ac.sitp.infrared.datasource.dao.AxrrAuditEvent;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface GlobalLogMapper {

    void insertAuditLog(AxrrAuditEvent log);

    List<AxrrAuditEvent> selectLogList(@Param("offset") int offset,
                                       @Param("page_size") int pageSize,
                                       @Param("begin_date") Date beginDate,
                                       @Param("end_date") Date endDate);
}

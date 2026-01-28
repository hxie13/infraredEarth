package cn.ac.sitp.infrared.datasource.mapper;

import cn.ac.sitp.infrared.datasource.dao.Algorithm;
import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.dao.Job;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface JobMapper {

    List<Job> selectJobList(@Param("offset") int offset,
                            @Param("page_size") int pageSize,
                            @Param("user") AxrrAccount user);

    int selectJobCount(@Param("user") AxrrAccount user);

    void insertJob(@Param("dataset_id") Long dataSetId,
                   @Param("algorithm_id") Long algorithmId,
                   @Param("user") AxrrAccount user);

    void updateJob(@Param("id") Long id,
                   @Param("job_status") String jobStatus);

    List<Algorithm> selectAlgorithmList(@Param("offset") int offset,
                                        @Param("page_size") int pageSize);

    int selectAlgorithmCount();
}

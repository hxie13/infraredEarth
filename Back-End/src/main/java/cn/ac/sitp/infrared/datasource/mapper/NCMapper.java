package cn.ac.sitp.infrared.datasource.mapper;

import cn.ac.sitp.infrared.datasource.dao.NC;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

public interface NCMapper {

    List<NC> selectNCList(@Param("offset") int offset,
                          @Param("page_size") int pageSize,
                          @Param("name") String name,
                          @Param("title") String title,
                          @Param("band_number") Integer bandNumber,
                          @Param("region_name") String regionName,
                          @Param("satellite_type") String satelliteType,
                          @Param("resolution") String resolution,
                          @Param("img_type") String imgType,
                          @Param("process_type") String processType,
                          @Param("begin_date") Date beginDate,
                          @Param("end_date") Date endDate);

    Long insertDataSet(@Param("user_no") Long userNo);

    void insertDataSetNc(@Param("dataset_id") Long dataSetId,
                         @Param("nc_id_list") List<Long> ncIdList);

    int selectNCCount(@Param("name") String name,
                      @Param("title") String title,
                      @Param("band_number") Integer bandNumber,
                      @Param("region_name") String regionName,
                      @Param("satellite_type") String satelliteType,
                      @Param("resolution") String resolution,
                      @Param("img_type") String imgType,
                      @Param("process_type") String processType,
                      @Param("begin_date") Date beginDate,
                      @Param("end_date") Date endDate);

    @Select("SELECT img_path FROM img WHERE nc_id = #{id}")
    String getImgPathById(@Param("id") Long id);

    @Select("SELECT distinct satellite_type FROM nc")
    List<String> selectNCSatelliteTypeList();

    @Select("SELECT distinct img_type FROM nc")
    List<String> selectNCImgTypeList();

    @Select("SELECT distinct process_type FROM nc")
    List<String> selectNCProcessTypeList();
}

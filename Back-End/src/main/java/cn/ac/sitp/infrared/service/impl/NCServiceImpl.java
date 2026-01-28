package cn.ac.sitp.infrared.service.impl;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.dao.NC;
import cn.ac.sitp.infrared.datasource.mapper.NCMapper;
import cn.ac.sitp.infrared.service.NCService;
import cn.ac.sitp.infrared.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NCServiceImpl implements NCService {
    private static final Log LOG = LogFactory.getLog(NCServiceImpl.class);

    @Autowired
    private NCMapper ncMapper;

    @Override
    public Map<String, Object> getNCList(int currPage, int pageSize, String name, String title, Integer bandNumber, String regionName,
                                         String satelliteType, String resolution, String imgType, String processType, Date beginDate,
                                         Date endDate) {
        Map<String, Object> contents = new HashMap<>();
        List<NC> ncList = ncMapper.selectNCList(pageSize * (currPage - 1), pageSize, name, title, bandNumber, regionName,
                satelliteType, resolution, imgType, processType, beginDate, endDate);
        contents.put("ncList", ncList);
        Util.buildPageModel(pageSize, ncMapper.selectNCCount(name, title, bandNumber, regionName, satelliteType,
                resolution, imgType, processType, beginDate, endDate), contents);
        contents.put("satelliteTypeList", ncMapper.selectNCSatelliteTypeList());
        contents.put("imgTypeList", ncMapper.selectNCImgTypeList());
        contents.put("processTypeList", ncMapper.selectNCProcessTypeList());
        return contents;
    }

    @Override
    public void addDataset(List<Long> ncIdList, AxrrAccount user) {
        Long dataSetId = ncMapper.insertDataSet(user.getUserno());
        ncMapper.insertDataSetNc(dataSetId, ncIdList);
    }

    @Override
    public InputStream getNcFileStream(Long id) throws Exception {
        String imgPath = ncMapper.getImgPathById(id);
        if (imgPath == null || imgPath.isEmpty()) {
            throw new FileNotFoundException("NC file not found for ID: " + id);
        }
        
        // 处理图片路径，将数据库中的绝对路径转换为相对路径
        String processedPath = imgPath;
        
        // 移除/data/前缀（根据数据库查询结果）
        if (processedPath.startsWith("/data/")) {
            processedPath = processedPath.substring(6); // 移除"/data/"
        }
        
        // 构建正确的本地文件路径
        // 从配置中获取基础路径，或使用默认路径
        String basePath = System.getProperty("user.dir");
        String fullPath = basePath + "/" + processedPath;
        
        // 检查文件是否存在
        java.io.File file = new java.io.File(fullPath);
        if (!file.exists()) {
            // 尝试其他可能的路径格式
            fullPath = "/data/" + processedPath;
            file = new java.io.File(fullPath);
            if (!file.exists()) {
                throw new FileNotFoundException("NC file not found at paths: " + imgPath + ", " + fullPath);
            }
        }
        
        return new FileInputStream(file);
    }

    @Override
    public Map<String, Object> getNCTypeList() {
        Map<String, Object> contents = new HashMap<>();
        contents.put("satelliteTypeList", ncMapper.selectNCSatelliteTypeList());
        contents.put("imgTypeList", ncMapper.selectNCImgTypeList());
        contents.put("processTypeList", ncMapper.selectNCProcessTypeList());
        return contents;
    }

}

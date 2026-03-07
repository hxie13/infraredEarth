package cn.ac.sitp.infrared.service.impl;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.dao.NC;
import cn.ac.sitp.infrared.datasource.mapper.NCMapper;
import cn.ac.sitp.infrared.service.NCService;
import cn.ac.sitp.infrared.service.StoredFileResolver;
import cn.ac.sitp.infrared.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NCServiceImpl implements NCService {
    private static final Log LOG = LogFactory.getLog(NCServiceImpl.class);

    @Autowired
    private NCMapper ncMapper;

    @Autowired
    private StoredFileResolver storedFileResolver;

    @Override
    public Map<String, Object> getNCList(int currPage, int pageSize, String name, String title, Integer bandNumber,
                                         String regionName, String satelliteType, String resolution, String imgType,
                                         String processType, Date beginDate, Date endDate) {
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
    @Transactional
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

        return Files.newInputStream(getNcFilePath(id));
    }

    @Override
    public Path getNcFilePath(Long id) throws Exception {
        String imgPath = ncMapper.getImgPathById(id);
        if (imgPath == null || imgPath.isEmpty()) {
            throw new FileNotFoundException("NC file not found for ID: " + id);
        }

        return storedFileResolver.resolveExistingFile(imgPath);
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

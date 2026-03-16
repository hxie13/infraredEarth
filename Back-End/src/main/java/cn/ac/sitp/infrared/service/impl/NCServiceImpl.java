package cn.ac.sitp.infrared.service.impl;

import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.datasource.dao.NC;
import cn.ac.sitp.infrared.datasource.mapper.NCMapper;
import cn.ac.sitp.infrared.service.NCService;
import cn.ac.sitp.infrared.service.StoredFileResolver;
import cn.ac.sitp.infrared.util.Util;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class NCServiceImpl implements NCService {

    private static final Logger log = LoggerFactory.getLogger(NCServiceImpl.class);
    private static final long TYPE_CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

    private final NCMapper ncMapper;
    private final StoredFileResolver storedFileResolver;

    // Lightweight cache for slowly-changing type lists
    private final AtomicReference<CachedTypeLists> typeCacheRef = new AtomicReference<>();

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
        // Use cached type lists instead of 3 separate queries per request
        appendTypeLists(contents);
        return contents;
    }

    @Override
    @Transactional
    public Long addDataset(List<Long> ncIdList, AxrrAccount user) {
        Long dataSetId = ncMapper.insertDataSet(user.getUserno());
        ncMapper.insertDataSetNc(dataSetId, ncIdList);
        return dataSetId;
    }

    @Override
    public InputStream getNcFileStream(Long id) throws Exception {
        // Reuse getNcFilePath to avoid duplicate getImgPathById query
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
        appendTypeLists(contents);
        return contents;
    }

    private void appendTypeLists(Map<String, Object> contents) {
        CachedTypeLists cached = typeCacheRef.get();
        if (cached == null || cached.isExpired()) {
            cached = new CachedTypeLists(
                    ncMapper.selectNCSatelliteTypeList(),
                    ncMapper.selectNCImgTypeList(),
                    ncMapper.selectNCProcessTypeList(),
                    System.currentTimeMillis()
            );
            typeCacheRef.set(cached);
        }
        contents.put("satelliteTypeList", cached.satelliteTypes);
        contents.put("imgTypeList", cached.imgTypes);
        contents.put("processTypeList", cached.processTypes);
    }

    private record CachedTypeLists(List<String> satelliteTypes, List<String> imgTypes,
                                   List<String> processTypes, long createdAt) {
        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > TYPE_CACHE_TTL_MS;
        }
    }
}

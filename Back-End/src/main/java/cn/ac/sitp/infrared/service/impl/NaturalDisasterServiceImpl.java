package cn.ac.sitp.infrared.service.impl;

import cn.ac.sitp.infrared.datasource.dao.Img;
import cn.ac.sitp.infrared.datasource.dao.NaturalDisaster;
import cn.ac.sitp.infrared.datasource.mapper.NaturalDisasterMapper;
import cn.ac.sitp.infrared.service.NaturalDisasterService;
import cn.ac.sitp.infrared.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NaturalDisasterServiceImpl implements NaturalDisasterService {
    private static final Log LOG = LogFactory.getLog(NaturalDisasterServiceImpl.class);

    @Autowired
    private NaturalDisasterMapper naturalDisasterMapper;

    @Override
    public Map<String, Object> getNaturalDisasterList(int currPage, int pageSize, Date beginDate, Date endDate,
                                                      String country, String place, String type) {
        Map<String, Object> contents = new HashMap<>();
        List<NaturalDisaster> naturalDisasterList = naturalDisasterMapper.selectNaturalDisasterList(pageSize * (currPage - 1),
                pageSize, beginDate, endDate, country, place, type);
        contents.put("naturalDisasterList", naturalDisasterList);
        Util.buildPageModel(pageSize, naturalDisasterMapper.selectNaturalDisasterCount(beginDate, endDate,
                country, place, type), contents);
        return contents;
    }

    @Override
    public Map<String, Object> getNaturalDisasterFileStream(Long id, Integer imgType) throws Exception {
        Map<String, Object> contents = new HashMap<>();
        List<Img> imgList= naturalDisasterMapper.getImgPathById(id, imgType);
        if (imgList == null || imgList.isEmpty()) {
            throw new FileNotFoundException("Natural disaster files not found for ID: " + id);
        }
        contents.put("imgList", imgList);
        return contents;
    }

    @Override
    public Map<String, Object> getNaturalDisasterTypeList() {
        Map<String, Object> contents = new HashMap<>();
        List<String> naturalDisasterTypeList = naturalDisasterMapper.selectNaturalDisasterTypeList();
        contents.put("naturalDisasterTypeList", naturalDisasterTypeList);
        return contents;
    }
}

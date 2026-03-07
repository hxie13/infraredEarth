package cn.ac.sitp.infrared.service.impl;

import cn.ac.sitp.infrared.datasource.dao.Img;
import cn.ac.sitp.infrared.datasource.dao.NaturalDisaster;
import cn.ac.sitp.infrared.datasource.mapper.NaturalDisasterMapper;
import cn.ac.sitp.infrared.service.NaturalDisasterService;
import cn.ac.sitp.infrared.service.StoredFileResolver;
import cn.ac.sitp.infrared.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NaturalDisasterServiceImpl implements NaturalDisasterService {

    @Autowired
    private NaturalDisasterMapper naturalDisasterMapper;

    @Autowired
    private StoredFileResolver storedFileResolver;

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
    public Map<String, Object> getNaturalDisasterFileData(Long id, Integer imgType) {
        List<Img> imgList = naturalDisasterMapper.getImgPathById(id, imgType);
        if (imgList != null) {
            for (Img img : imgList) {
                String imgPath = img.getImgPath();
                if (imgPath == null || imgPath.isBlank()) {
                    continue;
                }
                try {
                    img.setImgPath(storedFileResolver.resolveExistingFile(imgPath).toString());
                } catch (Exception ignored) {
                    // Keep the stored path unchanged so legacy records can still be retried by /rest/file.
                }
            }
        }
        Map<String, Object> contents = new HashMap<>();
        contents.put("imgList", imgList == null ? List.of() : imgList);
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

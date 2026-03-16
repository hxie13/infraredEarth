package cn.ac.sitp.infrared.datasource.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
public class NaturalDisaster implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String disasterNo;
    private String isHistoric;
    private String classificationKey;
    private String disasterGroup;
    private String disasterSubgroup;
    private String disasterType;
    private String disasterSubtype;
    private String externalIds;
    private String eventName;
    private String iso;
    private String country;
    private String subregion;
    private String region;
    private String location;
    private String origin;
    private String associatedTypes;
    private String isOFDAOrBHAResponse;
    private String isAppeal;
    private String isDeclaration;
    private Integer aidContribution;
    private BigDecimal magnitude;
    private String magnitudeScale;
    private String latitude;
    private String longitude;
    private String riverBasin;
    private Integer startYear;
    private Integer startMonth;
    private Integer startDay;
    private Integer endYear;
    private Integer endMonth;
    private Integer endDay;
    private Long totalDeaths;
    private Long noInjured;
    private Long noHomeless;
    private Long totalAffected;
    private Long reconstructionCosts;
    private Long reconstructionCostsAdjusted;
    private Long insuredDamage;
    private Long insuredDamageAdjusted;
    private Long totalDamage;
    private Long totalDamageAdjusted;
    private BigDecimal cpi;
    private String adminUnits;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private Date entryDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private Date lastUpdate;

    private String imgPath;
    private String description;

}

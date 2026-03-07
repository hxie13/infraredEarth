package cn.ac.sitp.infrared.web.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NcListRequest extends PaginationRequest {

    @JsonProperty("begin_date")
    private String beginDate;

    @JsonProperty("end_date")
    private String endDate;

    private String name;
    private String title;

    @JsonProperty("band_number")
    private Integer bandNumber;

    @JsonProperty("region_name")
    private String regionName;

    @JsonProperty("satellite_type")
    private String satelliteType;

    private String resolution;

    @JsonProperty("img_type")
    private String imgType;

    @JsonProperty("process_type")
    private String processType;
}

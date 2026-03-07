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
public class NaturalDisasterListRequest extends PaginationRequest {

    @JsonProperty("begin_date")
    private String beginDate;

    @JsonProperty("end_date")
    private String endDate;

    private String country;
    private String place;
    private String type;
}

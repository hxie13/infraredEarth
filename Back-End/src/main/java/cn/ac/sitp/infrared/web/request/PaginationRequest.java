package cn.ac.sitp.infrared.web.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginationRequest {

    @JsonProperty("curr_page")
    private Integer currPage;

    @JsonProperty("page_size")
    private Integer pageSize;
}

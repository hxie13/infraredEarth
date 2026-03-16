package cn.ac.sitp.infrared.datasource.dao;

import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class Algorithm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private String category;
    private String inputType;
    private String outputType;
    private String parametersSchema;
    private Boolean isActive;
    private Integer sortOrder;
}

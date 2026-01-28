package cn.ac.sitp.infrared.datasource.dao;

import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class China implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String gb;
    private String geometry;
}

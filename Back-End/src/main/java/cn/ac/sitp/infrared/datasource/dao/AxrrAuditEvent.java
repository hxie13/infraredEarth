package cn.ac.sitp.infrared.datasource.dao;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
public class AxrrAuditEvent implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 7381939453010636316L;

    private long id;

    private long version;

    private Date dt_insert;

    private Date dt_update;

    private Date dt_received;

    private Date dt_responsed;

    private String event_name;

    private String operation;

    private String request;

    private String requestChannel;

    private String requestSys;

    private String response;

    private String status;

    private String user_id;

    private String device_id;

    private String client_ip;

    private String description;

    private String source_server_id;

    private String target_server_id;

    private String group_name;

    private String display_name;

    private String user_name;

    private String case_number;

    private String exception;

    private String change;

    private int sent;

    private Long fk_device_no;

    private Long fk_cmd_result_no;

    private Long fk_data_result_no;

}

package cn.ac.sitp.infrared.datasource.dao;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
public class AxrrAuditEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 7381939453010636316L;

    private long id;

    private long version;

    private Date dtInsert;

    private Date dtUpdate;

    private Date dtReceived;

    private Date dtResponsed;

    private String eventName;

    private String operation;

    private String request;

    private String requestChannel;

    private String requestSys;

    private String response;

    private String status;

    private String userId;

    private String deviceId;

    private String clientIp;

    private String description;

    private String sourceServerId;

    private String targetServerId;

    private String groupName;

    private String displayName;

    private String userName;

    private String caseNumber;

    private String exception;

    private String change;

    private int sent;

    private Long fkDeviceNo;

    private Long fkCmdResultNo;

    private Long fkDataResultNo;

}

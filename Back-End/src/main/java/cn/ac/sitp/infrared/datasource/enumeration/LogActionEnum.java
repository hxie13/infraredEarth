package cn.ac.sitp.infrared.datasource.enumeration;

import lombok.Getter;

@Getter
public enum LogActionEnum {

    LOGIN("Login"),
    LOGOUT("Logout"),
    UPDATE_PASSWORD("Update password"),
    GET_LOG_LIST("Get log list"),
    GET_NC_LIST("Get NC list"),
    ADD_DATASET("Add data set"),
    GET_NATURAL_DISASTER_LIST("Get natural disaster list"),
    GET_JOB_LIST("Get job list"),
    GET_ALGORITHM_LIST("Get algorithm list"),
    GET_NC_IMG("Get NC img"),
    ADD_JOB("Add job"),
    GET_NATURAL_DISASTER_IMG("Get natural disaster img"),
    GET_NATURAL_DISASTER_TYPE_LIST("Get natural disaster type list"),
    GET_NC_TYPE_LIST("Get NC type list");

    final String description;

    LogActionEnum(String _desc) {
        this.description = _desc;
    }

}

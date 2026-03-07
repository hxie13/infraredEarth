package cn.ac.sitp.infrared.web.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterRequest {

    private String username;
    private String password;
    private String displayname;
    private String email;
    private String phone;
}

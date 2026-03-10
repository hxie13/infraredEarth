package cn.ac.sitp.infrared.web.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PasswordUpdateRequest {

    @NotBlank(message = "Old password is required")
    private String oldpassword;

    @NotBlank(message = "New password is required")
    private String password;
}

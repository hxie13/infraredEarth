package cn.ac.sitp.infrared.security;

import cn.ac.sitp.infrared.util.Util;
import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import java.io.PrintWriter;

public class CustomFormAuthenticationFilter extends FormAuthenticationFilter {

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        if (this.isLoginRequest(request, response)) {
            if (this.isLoginSubmission(request, response)) {
                return this.executeLogin(request, response);
            } else {
                return true;
            }
        } else {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(200);
            httpServletResponse.setContentType("application/json;charset=utf-8");

            PrintWriter out = httpServletResponse.getWriter();
            out.println(JSONObject.toJSONString(Util.noLogin()));
            out.flush();
            out.close();
            return false;
        }
    }
}

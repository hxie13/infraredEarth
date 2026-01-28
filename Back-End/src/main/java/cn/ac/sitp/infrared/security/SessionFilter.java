package cn.ac.sitp.infrared.security;

import cn.ac.sitp.infrared.util.Util;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.io.PrintWriter;

@WebFilter(filterName = "sessionFilter", urlPatterns = "/*")
@Order(1)
public class SessionFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String[] notifiers = {"/account/login", "/account/password"};
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        Subject currentUer = SecurityUtils.getSubject();
        boolean checkLogin = true;
        for (String url : notifiers) {
            if (request.getRequestURI().endsWith(url)) {
                checkLogin = false;
                break;
            }
        }

        if (checkLogin && (currentUer == null || !currentUer.isAuthenticated())) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            PrintWriter out = response.getWriter();
            out.append(JSON.toJSONString(Util.noLogin()));
            out.close();
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}

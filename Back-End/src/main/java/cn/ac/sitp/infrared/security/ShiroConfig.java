//package cn.ac.sitp.infrared.security;
//
//import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
//import cn.ac.sitp.infrared.service.AccountService;
//import jakarta.servlet.Filter;
//import org.apache.shiro.authc.*;
//import org.apache.shiro.authz.AuthorizationInfo;
//import org.apache.shiro.authz.SimpleAuthorizationInfo;
//import org.apache.shiro.realm.AuthorizingRealm;
//import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
//import org.apache.shiro.subject.PrincipalCollection;
//import org.apache.shiro.util.ThreadContext;
//import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//@Configuration
//public class ShiroConfig {
//    private static final Logger logger = LoggerFactory.getLogger(ShiroConfig.class);
//
//    @Bean(name = "userRealm")
//    public infraredShiroRealm myShiroRealm() {
//        return new infraredShiroRealm();
//    }
//
//    @Bean(name = "securityManager")
//    public DefaultWebSecurityManager securityManager(@Qualifier("userRealm") infraredShiroRealm userRealm) {
//        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
//        securityManager.setRealm(userRealm);
//        ThreadContext.bind(securityManager);//加上这句代码手动绑定
//        return securityManager;
//    }
//
//    /**
//     * 配置Shiro过滤器工厂
//     *
//     * @param securityManager 安全管理器
//     * @return ShiroFilterFactoryBean
//     */
//    @Bean
//    public ShiroFilterFactoryBean shiroFilter(@Qualifier("securityManager") DefaultWebSecurityManager securityManager) {
//        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
//
//        // 注册安全管理器
//        shiroFilterFactoryBean.setSecurityManager(securityManager);
//
//        Map<String, Filter> filters = shiroFilterFactoryBean.getFilters();
//        filters.put("authc", new CustomFormAuthenticationFilter());
//
//        // 定义资源访问规则
//        Map<String, String> map = new LinkedHashMap<>();
//
//        map.put("/rest/account/login", "anon");
//        map.put("/rest/account/password", "anon");
//        map.put("/rest/**", "authc");
//        shiroFilterFactoryBean.setFilterChainDefinitionMap(map);
//
//        shiroFilterFactoryBean.setLoginUrl("/rest/account/login");
//
//        return shiroFilterFactoryBean;
//    }
//
//    public class infraredShiroRealm extends AuthorizingRealm {
//
//        @Autowired
//        private AccountService accountService;
//
//        @Override
//        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
//            return new SimpleAuthorizationInfo();
//        }
//
//        @Override
//        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
//            UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
//            String username = token.getUsername();
//            String password = String.valueOf(token.getPassword());
//            AxrrAccount user = accountService.loginAccount(username, password);
//            return new SimpleAuthenticationInfo(user, password, user.getDisplayname());
//        }
//    }
//}

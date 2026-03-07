package cn.ac.sitp.infrared.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "cn.ac.sitp.infrared.datasource.mapper", sqlSessionFactoryRef = "infrareddbSqlSessionFactory")
public class DataSourceConfig {

    @Autowired
    private DataBaseProperties prop;

    @Bean(name = "infrareddbDS")
    @ConfigurationProperties(prefix = "spring.datasource.infrareddb")
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create().driverClassName(prop.driverClassName).url(prop.url)
                .username(prop.username).password(prop.password).build();
    }

    @Bean(name = "infrareddbSqlSessionFactory")
    @Primary
    public SqlSessionFactory infrareddbSqlSessionFactory(@Qualifier("infrareddbDS") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setTypeAliasesPackage("cn.ac.sitp.infrared.datasource.dao");
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapper/*.xml"));
        return bean.getObject();
    }

    @Primary
    @Bean(name = "infrareddbTransactionManger")
    public DataSourceTransactionManager infrareddbTransactionManger(@Qualifier("infrareddbDS") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


    @Bean(name = "infrareddbSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate infrareddbSqlSessionTemplate(
            @Qualifier("infrareddbSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}

package cn.siques.sample.server;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * 启动服务器并发布服务
 * @author Administrator
 */
@SpringBootApplication
@ComponentScan("cn.siques")
public class RpcBootstrap {
    static ConfigurableApplicationContext configurableApplicationContext;

    @Bean
    public ConfigurableApplicationContext configurableApplication (){
        return configurableApplicationContext;
    }

    public static void main(String[] args) {
        configurableApplicationContext  =SpringApplication.run(RpcBootstrap.class, args);
    }
}


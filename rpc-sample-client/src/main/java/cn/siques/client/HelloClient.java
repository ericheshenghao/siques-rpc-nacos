package cn.siques.client;


import cn.siques.api.HelloService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"cn.siques.client","cn.siques.register"})
public class HelloClient {



    public static void main(String[] args) throws Exception {

        ConfigurableApplicationContext context = SpringApplication.run(HelloClient.class, args);
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);


        /**
         * 测试 HelloService 接口的实现类 1
         */
        // 调用 RpcProxy 对象的 create 方法来创建 RPC 代理接口
        HelloService helloServiceImpl1 = rpcProxy.create(HelloService.class);
        // 调用 RPC 代理接口的方法(调用远程接口方法就像调用本地方法一样简单）
        String result = helloServiceImpl1.hello("Jack");
        System.out.println(result);

        /**
         * 测试 HelloService 接口的实现类 2
         */
        HelloService helloServiceImpl2 = rpcProxy.create(HelloService.class, "helloServiceImpl2");
        String result2 = helloServiceImpl2.hello("Tom");
        System.out.println(result2);



        System.exit(0);
    }
}

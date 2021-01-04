package cn.siques.sample.server;


import cn.siques.api.HelloService;
import cn.siques.server.RpcService;
import org.springframework.stereotype.Component;

@Component
@RpcService(interfaceName = HelloService.class)
public class HelloServiceImpl1 implements HelloService {

    @Override
    public String hello(String name) {
        return name + " Hello from " + "HelloServiceImpl1" ;
    }
}

package cn.siques.sample.server;

import cn.siques.api.HelloService;

import cn.siques.server.RpcService;
import org.springframework.stereotype.Component;

@Component
@RpcService(interfaceName = HelloService.class, serviceVersion = "helloServiceImpl2")
public class HelloServiceImpl2 implements HelloService {

    @Override
    public String hello(String name) {
        return name + " GoodBye from " + "HelloServiceImpl2" ;
    }
}

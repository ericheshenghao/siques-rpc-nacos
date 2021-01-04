package cn.siques.client;


import cn.siques.common.entity.RpcRequest;
import cn.siques.common.entity.RpcResponse;
import cn.siques.register.ServiceDiscovery;
import com.alibaba.nacos.api.naming.pojo.Instance;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * RPC 动态代理
 * 代理客户端进行建立连接，发送请求，接收请求（即屏蔽远程方法调用细节）
 */
@Component
public class RpcProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private Instance instance; // 服务

    @Autowired
    private ServiceDiscovery serviceDiscovery; // 服务发现组件


    /**
     * 对 send 方法进行增强
     * 使用示例：HelloService helloServiceImpl = rpcProxy.create(HelloService.class);
     * @param interfaceClass
     * @param <T>
     * @return 返回接口实例
     */
    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass) {
        return create(interfaceClass, "");
    }

    /**
     * 对 send 方法进行增强
     * 使用示例：HelloService helloService2 = rpcProxy.create(HelloService.class, "sample.hello2");
     * @param interfaceClass
     * @param serviceVersion
     * @param <T> 返回接口实例
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass, final String serviceVersion) {
        // 使用 CGLIB 动态代理机制
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(interfaceClass.getClassLoader());
        enhancer.setSuperclass(interfaceClass);
        enhancer.setCallback(new MethodInterceptor() {
            /**
             * @param o 被代理的对象（需要增强的对象）
             * @param method 被拦截的方法（需要增强的方法）
             * @param args 方法入参
             * @param methodProxy 用于调用原始方法
             * @return
             * @throws Throwable
             */
            @Override
            public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                // 创建 RPC 请求并设置属性
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setRequestId(UUID.randomUUID().toString());
                rpcRequest.setMethodName(method.getName());
                rpcRequest.setParameterTypes(method.getParameterTypes());
                rpcRequest.setParameters(args);
                rpcRequest.setInterfaceName(interfaceClass.getName());
                rpcRequest.setServiceVersion(serviceVersion);

                // 根据服务名称和版本号查询服务地址
                if (serviceDiscovery != null) {
                    String serviceName = interfaceClass.getName();
                    if (serviceVersion != null) {
                        String service_Version = serviceVersion.trim();
                        if (!StringUtils.isEmpty(service_Version)) {
                           serviceName += "-" + service_Version;
                        }
                    }
                    // 获取服务地址（用于建立连接）
                    instance = serviceDiscovery.discovery(serviceName);

                    LOGGER.info("discover service: {} => {}", serviceName, instance.getIp()+":"+instance.getPort());
                }

                if (instance != null) {
                    String serviceAddress = instance.getIp();
                    if (StringUtils.isEmpty(serviceAddress)) {
                        throw new RuntimeException("server address is empty");
                    }
                }

                // 从服务地址中解析主机名与端口号

                String host = instance.getIp();
                int port = instance.getPort();

                // 创建 RPC 客户端对象，建立连接/发送请求/接收请求
                RpcClient client = new RpcClient(host, port);
                long time = System.currentTimeMillis(); // 当前时间
                RpcResponse rpcResponse = client.send(rpcRequest);
                System.out.println(rpcResponse);
                LOGGER.info("time: {}ms", System.currentTimeMillis() - time);
                if (rpcResponse == null) {
                    throw new RuntimeException("response is null");
                }
                if (rpcResponse.hasException()) {
                    throw rpcResponse.getException();
                }
                else {
                    return rpcResponse.getResult();
                }
            }
        });

        return (T) enhancer.create();
    }


}

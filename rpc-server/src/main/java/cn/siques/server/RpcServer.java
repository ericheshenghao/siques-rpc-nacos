package cn.siques.server;


import cn.siques.register.ServiceRegistry;
import cn.siques.common.codec.RpcDecoder;
import cn.siques.common.codec.RpcEncoder;
import cn.siques.common.entity.RpcRequest;
import cn.siques.common.entity.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 */
@Component
public class RpcServer   {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    // 服务地址（比如服务被暴露在 Netty 的 8000 端口，服务地址就是 127.0.0.1:8000）

    @Value("${server.serviceAddress}")
    private String serviceAddress;

    // 服务注册组件（Nacos）
    @Autowired
    private ServiceRegistry serviceRegistry;

    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    // 存储服务名称与服务对象之间的映射关系
    private Map<String, Object> handlerMap = new HashMap<>();




    /**
     * 在依赖注入完成后会自动执行该方法
     * 该方法的目标就是启动 Netty 服务器进行服务端和客户端的通信，接收并处理客户端发来的请求,
     * 并且还要将服务名称和服务地址注册进 Zookeeper（注册中心）
     * @throws Exception
     */
    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new RpcDecoder(RpcRequest.class)); // 解码器
                    pipeline.addLast(new RpcEncoder(RpcResponse.class)); // 编码器
                    pipeline.addLast(new RpcServerHandler(handlerMap)); // 处理 RPC 请求
                }
            });
            // 获取服务地址/端口号，建立连接
            String[] addressArray = StringUtils.split(serviceAddress, ":");
            String ip = addressArray[0];
            int port = Integer.parseInt(addressArray[1]);
            ChannelFuture future = serverBootstrap.bind(ip, port).sync();

            // 注册服务到nacos
            if (serviceRegistry != null) {
               for (String interfaceName : handlerMap.keySet()) {
                   serviceRegistry.register(interfaceName, addressArray);
                   LOGGER.info("register service: {} => {}", interfaceName, serviceAddress);
               }
            }
            LOGGER.info("server started on port {}", port);

            // 关闭 RPC 服务器
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


    /**
     *  在依赖注入完成后会自动执行该方法
     * 该方法的作用就是获取带有 @RpcSerivce 注解的类的 value (被暴露的实现类的接口名称) 和 version (被暴露的实现类的版本号，默认为 “”)
     */

    @PostConstruct
    public void onApplicationEvent( ) {
        Map<String, Object> serviceBeanMap = configurableApplicationContext.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean: serviceBeanMap.values()) {
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                // 获取服务名称
                String serviceName = rpcService.interfaceName().getName();
                // 获取服务版本
                String serviceVersion = rpcService.serviceVersion();
                if(serviceVersion != null){
                    serviceVersion = serviceVersion.trim();
                    if(!StringUtils.isEmpty(serviceVersion)){
                        serviceName = serviceName + "-" + serviceVersion;
                    }
                }
                handlerMap.put(serviceName, serviceBean);
            }
        }

    }
}

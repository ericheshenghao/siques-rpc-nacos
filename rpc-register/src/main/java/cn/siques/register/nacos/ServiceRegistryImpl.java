package cn.siques.register.nacos;

import cn.siques.register.ServiceRegistry;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author : heshenghao
 * @date : 15:11 2021/1/4
 */
@Component
public class ServiceRegistryImpl implements ServiceRegistry {

    @Value("${nacos.discovery.server-addr}")
    String serverList;

    @Override
    public void register(String serviceName, String[] serviceAddress) throws NacosException {
        NamingService naming = NamingFactory.createNamingService(serverList);
        naming.registerInstance(serviceName,serviceAddress[0], Integer.parseInt(serviceAddress[1]));
    }
}

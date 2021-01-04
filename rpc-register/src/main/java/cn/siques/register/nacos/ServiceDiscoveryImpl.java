package cn.siques.register.nacos;

import cn.siques.register.ServiceDiscovery;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


/**
 * @author : heshenghao
 * @date : 15:10 2021/1/4
 */
@Component
public class ServiceDiscoveryImpl implements ServiceDiscovery {

    @Value("${nacos.discovery.server-addr}")
    String serverList;

    @Override
    public Instance discovery(String serviceName) throws NacosException {
        NamingService naming = NamingFactory.createNamingService(serverList);
        Instance instance = naming.selectOneHealthyInstance(serviceName);
        return instance;
    }
}

package cn.siques.register;


import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.springframework.stereotype.Component;

/**
 * 服务发现接口
 */
public interface ServiceDiscovery {

    /**
     * 根据服务名称查找服务地址
     * @param serviceName 服务名称（被暴露的实现类的接口名称）
     * @return
     */
    Instance discovery(String serviceName) throws NacosException;
}

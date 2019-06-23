package cn.tenbit.wolf.web.core.apiroute;

import cn.tenbit.wolf.web.core.model.WebRoute;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author bangquan.qian
 * @Date 2019-05-05 15:38
 */
@Component
public class ApiRoute {

    private static final Map<String, WebRoute> ROUTE = new ConcurrentHashMap<>();

    //--------------------------------------------------------------------------------------------//

    static void addRoute(String serviceKey, String methodKey, String serviceName, String methodName) {
        ROUTE.put(ApiRouteUtils.generateKey(serviceKey, methodKey), WebRoute.of(serviceKey, methodKey, serviceName, methodName));
    }

    public static WebRoute getRoute(String key) {
        return ROUTE.get(key);
    }
}

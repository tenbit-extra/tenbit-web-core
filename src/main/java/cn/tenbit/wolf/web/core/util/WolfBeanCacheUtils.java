package cn.tenbit.wolf.web.core.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author bangquan.qian
 * @Date 2019-01-05 22:58
 */
@Slf4j
public class WolfBeanCacheUtils {

    private static final Map<String, Object> beans = new ConcurrentHashMap<>(1024);

    public static void set(String name, Object bean) {
        if (name == null || bean == null) {
            return;
        }

        try {
            beans.put(name, bean);
        } catch (Exception e) {
            log.error("set", e);
        }
    }

    public static Object get(String name) {
        if (name == null) {
            return null;
        }

        try {
            return beans.get(name);
        } catch (Exception e) {
            log.error("get", e);
        }
        return null;
    }

}

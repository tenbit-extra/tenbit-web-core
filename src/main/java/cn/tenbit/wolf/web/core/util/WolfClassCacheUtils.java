package cn.tenbit.wolf.web.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author bangquan.qian
 * @Date 2019-05-07 16:25
 */
@Slf4j
public class WolfClassCacheUtils {

    private static final Map<String, Class> caches = new ConcurrentHashMap<>();

    public static Class loadClass(String name) throws ClassNotFoundException {
        Class<?> clz = caches.get(name);
        if (clz == null) {
            clz = ClassUtils.getClass(name);
            if (clz != null) {
                caches.put(name, clz);
            }
        }
        return clz;
    }
}

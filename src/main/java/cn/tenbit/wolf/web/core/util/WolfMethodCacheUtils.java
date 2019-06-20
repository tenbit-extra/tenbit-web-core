package cn.tenbit.wolf.web.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author bangquan.qian
 * @Date 2019-01-05 23:46
 */
@Slf4j
public class WolfMethodCacheUtils {

    private static final Map<String, Method> methods = new ConcurrentHashMap<>(2048);

    private static final String KEY_SPILT = "#";

    public static String generateKey(String serviceName, String methodName) {
        if (StringUtils.isBlank(serviceName) || StringUtils.isBlank(methodName)) {
            return null;
        }
        return serviceName + KEY_SPILT + methodName;
    }

    public static void set(String key, Method method) {
        if (key == null || method == null) {
            return;
        }

        try {
            methods.put(key, method);
        } catch (Exception e) {
            log.error("set", e);
        }
    }

    public static Method get(String key) {
        if (key == null) {
            return null;
        }

        try {
            /*Method method = methods.get(key);
            if (method != null) {
                return ReflectionFactory.getReflectionFactory().copyMethod(method);
            }*/
            return methods.get(key);
        } catch (Exception e) {
            log.error("get", e);
        }
        return null;
    }
}

package cn.tenbit.wolf.web.core.apiroute;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author bangquan.qian
 * @Date 2019-05-06 11:39
 */
@Slf4j
public class ApiRouteUtils {

    public static String generateKey(String serviceKey, String methodKey) {
        return StringUtils.joinWith("/", serviceKey, methodKey);
    }
}

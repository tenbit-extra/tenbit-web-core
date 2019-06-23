package cn.tenbit.wolf.web.core.util;

import cn.tenbit.hare.core.common.constant.HareConsts;
import cn.tenbit.hare.core.exception.HareException;
import cn.tenbit.hare.core.util.*;
import cn.tenbit.wolf.soa.core.response.SoaResponse;
import cn.tenbit.wolf.web.core.constant.WolfWebConsts;
import cn.tenbit.wolf.web.core.exception.WolfWebErrorCode;
import cn.tenbit.wolf.web.core.model.*;
import cn.tenbit.wolf.web.core.support.WolfSpringBeans;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author bangquan.qian
 * @Date 2019-04-29 10:48
 */
@Slf4j
public class WolfWebUtils {

    public static WebContextAndResult printHandle(WebContextAndResult wcar) {
        printWebResult(wcar.getInside().getResponse(), wcar.getResult(), WolfWebConsts.HTTP_HEADER_DEFAULT_CONTENT_TYPE_VALUE, false);
        return wcar;
    }

    public static void printWebResult(HttpServletResponse response, Object result, String contentType, boolean isPrettyFormat) {
        boolean hasAttachment = false;
        if (result instanceof WebResult) {
            Object data = ((WebResult) result).getData();
            if (data instanceof WebAttachment) {
                hasAttachment = true;
            }
        }

        if (hasAttachment) {
            WebAttachment attachment = (WebAttachment) ((WebResult) result).getData();
            handleAttachment(response, attachment);
        } else {
            handleResponse(response, result, contentType, isPrettyFormat);
        }
    }

    public static void handleResponse(HttpServletResponse response, Object result, String contentType, boolean isPrettyFormat) {
        try (PrintWriter pw = response.getWriter()) {
            response.setCharacterEncoding(WolfWebConsts.DEFAULT_CHARSET);
            response.setHeader(WolfWebConsts.HTTP_HEADER_CONTENT_TYPE_KEY, WolfWebConsts.HTTP_HEADER_DEFAULT_CONTENT_TYPE_VALUE);
            if (StringUtils.isNotBlank(contentType)) {
                response.setHeader(WolfWebConsts.HTTP_HEADER_CONTENT_TYPE_KEY, contentType);
            }

            if (result == null) {
                return;
            }
            if (result instanceof String) {
                pw.print(result);
                return;
            }

            SerializerFeature[] features = BooleanUtils.isTrue(isPrettyFormat) ? WolfWebConsts.PRETTY_SERIALIZER_FEATURES : WolfWebConsts.NORMAL_SERIALIZER_FEATURES;

            pw.print(HareJsonUtils.toJsonString(result, features));
        } catch (Throwable e) {
            throw HareException.of("handleResponse", e);
        }
    }

    private static void handleAttachment(HttpServletResponse response, WebAttachment attachment) {
        if (attachment == null) {
            return;
        }

        try (ServletOutputStream os = response.getOutputStream()) {
            response.setContentType(WolfWebConsts.HTTP_HEADER_XLS_CONTENT_TYPE_VALUE);
            response.setHeader(WolfWebConsts.HTTP_HEADER_CONTENT_DISPOSITION_KEY,
                    WolfWebConsts.HTTP_HEADER_CONTENT_DISPOSITION_ATTACHMENT_VALUE + URLEncoder.encode(attachment.getName(), WolfWebConsts.DEFAULT_CHARSET));

            IOUtils.write(attachment.getContent(), os);
        } catch (Exception e) {
            throw HareException.of("handleAttachment", e);
        }
    }

    public static WebContextAndResult errorHandle(WebContextAndResult wcar, Throwable e) {
        WebInside inside = wcar.getInside();
        inside.setThrowable(e);

        WolfWebUtils.logError(wcar, e);

        return wcar;
    }

    private static void logError(WebContextAndResult wcar, Throwable e) {
        log.error("handle, traceId:{}, serviceKey:{}, methodKey:{}, webContext:{}, exception:{}",
                WolfWebUtils.getTraceId(wcar),
                wcar.getInside().getRoute().getServiceKey(),
                wcar.getInside().getRoute().getMethodKey(),
                HareJsonUtils.toJsonString(wcar),
                e);
    }

    private static String getTraceId(WebContextAndResult wcar) {
        String traceId = wcar.getInside().getTraceId();
        if (StringUtils.isBlank(traceId)) {
            wcar.getInside().setTraceId(traceId = HareRandomUtils.uuid());
        }
        return traceId;
    }

    public static WebContextAndResult initHandle(HttpServletRequest request, HttpServletResponse response,
                                                 String serviceKey, String methodKey, WebContextAndResult wcar) {

        WebInside inside = wcar.getInside();
        inside.setRequest(request);
        inside.setResponse(response);

        JSONObject rawParam = WolfServletUtils.parseParamFromRequest(request);
        inside.setRawParam(rawParam);

        List<WebAttachment> attachments = WolfServletUtils.parseMultipartRequest(request);
        inside.setAttachments(attachments);

        WebRoute route = inside.getRoute();
        route.setServiceKey(serviceKey);
        route.setMethodKey(methodKey);

        WebStatistics statistics = inside.getStatistics();
        statistics.setStartTime(HareTimeUtils.currentTimeMillis());

        WebContext context = wcar.getContext();
        WebResult result = wcar.getResult();

        return wcar;
    }

    public static WebContextAndResult doHandle(WebContextAndResult wcar) {
        return wrap(wcar, invoke(wcar));
    }

    @SuppressWarnings(HareConsts.SUPPRESS_WARNING_UNCHECKED)
    private static WebContextAndResult wrap(WebContextAndResult wcar, SoaResponse soaResponse) {
        WebResult result = wcar.getResult();
        result.setCode(soaResponse.getCode());
        result.setData(soaResponse.getData());
        return wcar;
    }

    private static SoaResponse invoke(WebContextAndResult wcar) {
        WebInside inside = wcar.getInside();
        WebRoute route = inside.getRoute();
        return doInvoke(route.getServiceName(), route.getMethodName(), inside.getRawParam());
    }

    private static SoaResponse doInvoke(String serviceName, String methodName, Object inputParam) {
        if (inputParam == null) {
            HarePrintUtils.jsonConsole(HareTimeUtils.currentFormatTime(), "inputParam", serviceName, methodName);
            return new SoaResponse<>();
        }

        Object bean = getService(serviceName);
        if (bean == null) {
            HarePrintUtils.jsonConsole(HareTimeUtils.currentFormatTime(), "getService", serviceName, methodName, inputParam);
            return new SoaResponse<>();
        }

        List<Object> args = new ArrayList<>();
        Method serviceMethod = getMethodAndFillArgs(serviceName, methodName, inputParam, bean, args);
        if (serviceMethod == null) {
            HarePrintUtils.jsonConsole(HareTimeUtils.currentFormatTime(), "getMethodAndFillArgs", serviceName, methodName, inputParam);
            return new SoaResponse<>();
        }

        SoaResponse result = null;
        try {
            result = (SoaResponse) serviceMethod.invoke(bean, args.toArray());
        } catch (Exception e) {
            throw HareException.of(WolfWebErrorCode.INVOKE_ERROR, e);
        } finally {
            String className = bean == null ? HareConsts.EMPTY : bean.getClass().getSimpleName();
            HarePrintUtils.jsonConsole(HareTimeUtils.currentFormatTime(), className, methodName, inputParam);
        }
        if (result == null) {
            return new SoaResponse<>();
        }

        return result;
    }

    private static Method getMethodAndFillArgs(String serviceName, String methodName, Object inputParam, Object bean, List<Object> args) {
        String cacheMethodKey = WolfMethodCacheUtils.generateKey(serviceName, methodName);
        Method serviceMethod = WolfMethodCacheUtils.get(cacheMethodKey);
        if (serviceMethod != null) {
            fillArgs(serviceMethod, inputParam, args);
        } else {
            try {
                serviceMethod = findServiceMethod(bean, methodName, inputParam, args);
            } catch (Exception e) {
                if (e instanceof HareException) {
                    throw (HareException) e;
                }
                throw HareException.of(WolfWebErrorCode.API_ROUTE_ERROR, e);
            }
            if (serviceMethod != null) {
                WolfMethodCacheUtils.set(cacheMethodKey, serviceMethod);
            }
        }
        return serviceMethod;
    }

    @SuppressWarnings(HareConsts.SUPPRESS_WARNING_UNCHECKED)
    private static Object getService(String serviceName) {
        Object bean = WolfBeanCacheUtils.get(serviceName);
        if (bean != null) {
            return bean;
        }
        try {
            bean = WolfSpringBeans.getBeanByType(WolfClassCacheUtils.loadClass(serviceName));
        } catch (Exception e) {
            throw HareException.of(WolfWebErrorCode.API_ROUTE_ERROR, e);
        }
        if (bean != null) {
            WolfBeanCacheUtils.set(serviceName, bean);
        }
        return bean;
    }

    private static Method findServiceMethod(Object bean, String methodName, Object inputParam, List<Object> params) throws Exception {
        if (bean == null || inputParam == null) {
            return null;
        }

        String parameterInput = String.valueOf(inputParam);

        for (Class<?> beanInterface : bean.getClass().getInterfaces()) {
            Method method = doFindServiceMethod(methodName, params, parameterInput, beanInterface.getMethods());
            if (method != null) {
                return method;
            }
        }

        return doFindServiceMethod(methodName, params, parameterInput, bean.getClass().getMethods());
    }

    private static Method doFindServiceMethod(String methodName, List<Object> params, String parameterInput, Method[] methods) {
        for (Method method : methods) {
            if (!methodName.equals(method.getName())) {
                continue;
            }

            params.clear();

            Type[] types = method.getGenericParameterTypes();
            List<String> paramList = new ArrayList<String>(types.length);
            if (types.length == 1) {
                paramList.add(parameterInput);
            }
            if (types.length > 1) {
                paramList.addAll(HareJsonUtils.parseJavaArray(parameterInput, String.class));
            }
            if (types.length == paramList.size()) {
                for (int i = 0; i < types.length; i++) {
                    String param = paramList.get(i);
                    if (StringUtils.isBlank(param)) {
                        param = null;
                    }
                    Object paramObj = null;
                    try {
                        paramObj = JSON.parseObject(param, types[i]);
                    } catch (Exception e) {
                        throw HareException.of(WolfWebErrorCode.INSIDE_ERROR, e);
                    }
                    params.add(paramObj);
                }
                return method;
            }
        }
        return null;
    }

    private static void fillArgs(Method serviceMethod, Object inputParam, List<Object> args) {
        if (serviceMethod == null || inputParam == null) {
            return;
        }

        args.clear();

        String parameterInput = String.valueOf(inputParam);
        Type[] types = serviceMethod.getGenericParameterTypes();
        List<String> paramList = new ArrayList<>(types.length);
        if (types.length == 1) {
            paramList.add(parameterInput);
        }
        if (types.length > 1) {
            paramList.addAll(HareJsonUtils.parseJavaArray(parameterInput, String.class));
        }
        if (types.length == paramList.size()) {
            for (int i = 0; i < types.length; i++) {
                String param = paramList.get(i);
                if (StringUtils.isBlank(param)) {
                    param = null;
                }
                args.add(JSON.parseObject(param, types[i]));
            }
        }
    }

    public static WebContextAndResult finalHandle(WebContextAndResult wcar) {
        WebInside inside = wcar.getInside();
        WebResult result = wcar.getResult();
        WebStatistics statistics = inside.getStatistics();

        int dataLength = getDataLength(result.getData());
        statistics.setDataLength(dataLength);

        Long startTime = HareObjectUtils.defaultIfNull(statistics.getStartTime(), HareTimeUtils.currentTimeMillis());
        Long endTime = HareTimeUtils.currentTimeMillis();
        Long costTime = endTime - startTime;

        statistics.setStartTime(startTime);
        statistics.setEndTime(endTime);
        statistics.setCostTime(costTime);

        boolean success = inside.getThrowable() == null;
        result.setSuccess(success);
        if (!success) {
            result.setMessage(HareStringUtils.toNotNullString(inside.getThrowable().getMessage()));
        }

        WebViewStatistics viewStatistics = result.getStatistics();
        viewStatistics.setTrace(inside.getTraceId());
        viewStatistics.setCost(HareStringUtils.toNotNullString(costTime));
        viewStatistics.setLength(HareStringUtils.toNotNullString(dataLength));

        result.setTimestamp(HareTimeUtils.currentTimeMillisString());
        return wcar;
    }

    private static int getDataLength(Object data) {
        if (data == null) {
            return 0;
        }
        try {
            String str = HareJsonUtils.toJsonString(data);
            return HareStringUtils.toNotNullString(str).length();
        } catch (Exception e) {
            log.error("getDataLength", e);
        }
        return 0;
    }
}

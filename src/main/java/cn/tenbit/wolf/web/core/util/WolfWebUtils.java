package cn.tenbit.wolf.web.core.util;

import cn.tenbit.hare.core.exception.HareException;
import cn.tenbit.hare.core.util.*;
import cn.tenbit.wolf.soa.core.response.SoaResponse;
import cn.tenbit.wolf.web.core.constant.WolfWebConsts;
import cn.tenbit.wolf.web.core.model.*;
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

    private static WebContextAndResult wrap(WebContextAndResult wcar, SoaResponse invoke) {
        return null;
    }

    private static SoaResponse invoke(WebContextAndResult wcar) {
        WebInside inside = wcar.getInside();
        WebRoute route = inside.getRoute();
        return doInvoke(route.getServiceName(), route.getMethodName(), inside.getRawParam());
    }

    private static SoaResponse doInvoke(String serviceName, String methodName, Object inputParam) {
        if (inputParam == null) {
            HarePrintUtils.jsonConsole(HareTimeUtils.current(), "inputParam", serviceName, methodName);
            return new SoaResponse<>();
        }

        Object bean = getService(serviceName);
        if (bean == null) {
            JxPrintUtils.printJsonString(JxTimeUtils.current(), "getService", serviceName, methodName, inputParam);
            return new SoaResponse<>();
        }

        List<Object> args = new ArrayList<>();
        Method serviceMethod = getMethodAndFillArgs(serviceName, methodName, inputParam, bean, args);
        if (serviceMethod == null) {
            JxPrintUtils.printJsonString(JxTimeUtils.current(), "getMethodAndFillArgs", serviceName, methodName, inputParam);
            return new SoaResponse<>();
        }

        SoaResponse result = null;
        try {
            result = (SoaResponse) serviceMethod.invoke(bean, args.toArray());
        } catch (Exception e) {
            throw new JxWebException(JxWebErrorCode.SOA_RESPONSE_ERROR, e);
        } finally {
            String className = bean == null ? JxWebConsts.EMPTY_STRING : bean.getClass().getSimpleName();
            JxPrintUtils.printJsonString(JxTimeUtils.current(), className, methodName, inputParam);
        }
        if (result == null) {
            return new SoaResponse<>();
        }

        return result;
    }

    public static WebContextAndResult finalHandle(WebContextAndResult wcar) {
        WebInside inside = wcar.getInside();
        WebStatistics statistics = inside.getStatistics();

        Long startTime = HareObjectUtils.defaultIfNull(statistics.getStartTime(), HareTimeUtils.currentTimeMillis());
        Long endTime = HareTimeUtils.currentTimeMillis();
        Long costTime = endTime - startTime;

        statistics.setStartTime(startTime);
        statistics.setEndTime(endTime);
        statistics.setCostTime(costTime);

        return wcar;
    }
}

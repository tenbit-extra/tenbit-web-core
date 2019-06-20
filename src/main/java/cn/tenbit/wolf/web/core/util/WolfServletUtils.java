package cn.tenbit.wolf.web.core.util;

import cn.tenbit.wolf.web.core.constant.WolfWebConsts;
import cn.tenbit.wolf.web.core.model.WebAttachment;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Author bangquan.qian
 * @Date 2019-04-30 14:28
 */
@Slf4j
public class WolfServletUtils {

    public static JSONObject parseParamFromRequest(HttpServletRequest request) {
        JSONObject jsonObject = null;
        try {
            return doParseParamFromRequest(request);
        } catch (Exception e) {
            log.error("parseParamFromRequest", e);
        }
        return jsonObject;
    }

    private static JSONObject doParseParamFromRequest(HttpServletRequest request) throws Exception {
        String paramInput = null;

        Map<String, String[]> parameterMap = request.getParameterMap();
        if (MapUtils.isNotEmpty(parameterMap)) {
            Map<String, Object> params = new TreeMap<>();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String key = entry.getKey();
                String[] value = entry.getValue();
                if (ArrayUtils.isNotEmpty(value)) {
                    params.put(key, value.length > 1 ? value : value[0]);
                }
            }
            paramInput = JSON.toJSONString(params);
        }

        if (StringUtils.isBlank(paramInput)) {
            Map<String, Object> params = WebUtils.getParametersStartingWith(request, null);
            if (MapUtils.isNotEmpty(params)) {
                paramInput = JSON.toJSONString(params);
            }
        }

        if (StringUtils.isBlank(paramInput)) {
            paramInput = IOUtils.toString(request.getInputStream(), WolfWebConsts.DEFAULT_CHARSET);
        }

        return StringUtils.isNotBlank(paramInput) ? JSON.parseObject(paramInput) : new JSONObject();
    }

    public static List<WebAttachment> parseMultipartRequest(HttpServletRequest request) {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        boolean isContainFile = multipartResolver.isMultipart(request);
        if (!isContainFile) {
            return Collections.emptyList();
        }

        Map<String, MultipartFile> multipartFileMap = ((MultipartHttpServletRequest) request).getFileMap();
        List<WebAttachment> attachments = new ArrayList<>(multipartFileMap.size());
        for (Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
            CommonsMultipartFile multipartFile = (CommonsMultipartFile) entry.getValue();
            if (multipartFile == null || multipartFile.getFileItem() == null) {
                continue;
            }

            WebAttachment attachment = new WebAttachment();
            attachment.setContent(multipartFile.getBytes());
            attachment.setName(multipartFile.getFileItem().getName());

            attachments.add(attachment);
        }
        return attachments;
    }
}

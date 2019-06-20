package cn.tenbit.wolf.web.core.constant;

import cn.tenbit.wolf.soa.core.model.EmptyObject;
import cn.tenbit.wolf.soa.core.response.SoaResponse;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @Author bangquan.qian
 * @Date 2019-04-25 15:17
 */

public interface WolfWebConsts {

    SoaResponse EMPTY_SOA_RESPONSE = new SoaResponse();

    EmptyObject EMPTY_OBJECT = new EmptyObject();

    String DEFAULT_CHARSET = "utf-8";

    String HTTP_HEADER_CONTENT_DISPOSITION_KEY = "Content-Disposition";
    String HTTP_HEADER_CONTENT_DISPOSITION_ATTACHMENT_VALUE = "attachment;name=";

    String HTTP_HEADER_CONTENT_TYPE_KEY = "Content-Type";
    String HTTP_HEADER_XLS_CONTENT_TYPE_VALUE = "application/x-xls;charset=utf-8";
    String HTTP_HEADER_DEFAULT_CONTENT_TYPE_VALUE = "application/json;charset=utf-8";
    String HTTP_HEADER_TEXT_PLAIN_CONTENT_TYPE_VALUE = "text/plain";

    SerializerFeature[] NORMAL_SERIALIZER_FEATURES = {
            SerializerFeature.DisableCircularReferenceDetect,
            SerializerFeature.WriteMapNullValue
    };

    SerializerFeature[] PRETTY_SERIALIZER_FEATURES = {
            SerializerFeature.PrettyFormat,
            SerializerFeature.DisableCircularReferenceDetect,
            SerializerFeature.WriteMapNullValue
    };
}

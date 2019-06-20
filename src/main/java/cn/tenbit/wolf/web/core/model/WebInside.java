package cn.tenbit.wolf.web.core.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.List;

/**
 * @Author bangquan.qian
 * @Date 2019-06-19 17:24
 */
@Getter
@Setter
@NoArgsConstructor
public class WebInside implements Serializable {
    private static final long serialVersionUID = -4203242421831502692L;

    private transient HttpServletRequest request;

    private transient HttpServletResponse response;

    private final WebRoute route = new WebRoute();

    private final WebStatistics statistics = new WebStatistics();

    private String traceId;

    private Throwable throwable;

    private JSONObject rawParam;

    private List<WebAttachment> attachments;
}

package cn.tenbit.wolf.web.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.Serializable;

/**
 * @Author bangquan.qian
 * @Date 2019-06-19 16:43
 */
@Getter
@Setter
@NoArgsConstructor
public class WebResult<T> implements Serializable {
    private static final long serialVersionUID = 8715914170739829405L;

    public static final Long DEFAULT_CODE = NumberUtils.LONG_ZERO;

    public static final String DEFAULT_MESSAGE = "success";

    private Long code = DEFAULT_CODE;

    private String message = DEFAULT_MESSAGE;

    private T data;

    private String timestamp;
}

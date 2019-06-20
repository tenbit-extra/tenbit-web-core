package cn.tenbit.wolf.web.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author bangquan.qian
 * @Date 2019-06-19 18:40
 */
@Getter
@Setter
@NoArgsConstructor
public class WebContextAndResult implements Serializable {
    private static final long serialVersionUID = 4388914824836953692L;

    private final WebContext context = new WebContext();

    private final WebInside inside = new WebInside();

    private final WebResult result = new WebResult();
}

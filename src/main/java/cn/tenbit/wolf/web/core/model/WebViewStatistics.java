package cn.tenbit.wolf.web.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author bangquan.qian
 * @Date 2019-06-20 16:11
 */
@Getter
@Setter
@NoArgsConstructor
public class WebViewStatistics implements Serializable {
    private static final long serialVersionUID = -1040693580545530300L;

    private String length;

    private String cost;

    private String trace;
}

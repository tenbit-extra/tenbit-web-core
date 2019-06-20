package cn.tenbit.wolf.web.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author bangquan.qian
 * @Date 2019-05-05 15:35
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class WebRoute implements Serializable {
    private static final long serialVersionUID = 6390028984892005420L;

    private String serviceKey;
    private String methodKey;

    private String serviceName;
    private String methodName;
}

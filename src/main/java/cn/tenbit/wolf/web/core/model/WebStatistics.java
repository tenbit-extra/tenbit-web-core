package cn.tenbit.wolf.web.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author bangquan.qian
 * @Date 2019-01-14 15:22
 */
@Getter
@Setter
@NoArgsConstructor
public class WebStatistics implements Serializable {
    private static final long serialVersionUID = -3777847678125354489L;

    /**
     * 数据data长度
     */
    private Integer dataLength;

    /**
     * 执行开始时间
     */
    private Long startTime;

    /**
     * 执行结束时间
     */
    private Long endTime;

    /**
     * 执行过程耗时（毫秒）
     */
    private Long costTime;
}

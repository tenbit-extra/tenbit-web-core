package cn.tenbit.wolf.web.core.exception;

import cn.tenbit.hare.core.exception.HareExecCode;

/**
 * @Author bangquan.qian
 * @Date 2019-06-20 14:18
 */
public interface WolfWebErrorCode {

    HareExecCode INSIDE_ERROR = HareExecCode.newFailure("内部错误");
    HareExecCode REQUEST_ERROR = HareExecCode.newFailure("请求错误");
    HareExecCode INVOKE_ERROR = HareExecCode.newFailure("执行错误");
    HareExecCode API_ROUTE_ERROR = HareExecCode.newFailure("路由错误");
}

package cn.tenbit.wolf.web.core.support;

import cn.tenbit.wolf.web.core.controller.AbstractWolfWebController;
import cn.tenbit.wolf.web.core.model.WebContextAndResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author bangquan.qian
 * @Date 2019-06-21 16:59
 */
public class WolfWebControllerSupport extends AbstractWolfWebController {

    @Override
    protected boolean needDirectHandle(HttpServletRequest request, HttpServletResponse response, String serviceKey, String methodKey) {
        return false;
    }

    @Override
    protected void directHandle(HttpServletRequest request, HttpServletResponse response, String serviceKey, String methodKey) {
    }

    @Override
    protected void beforeHandle(WebContextAndResult context) {
    }

    @Override
    protected void afterHandle(WebContextAndResult context) {
    }

    @Override
    protected void lastHandle(WebContextAndResult context) {
    }
}

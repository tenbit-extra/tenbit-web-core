package cn.tenbit.wolf.web.core.controller;

import cn.tenbit.hare.core.execintercept.HareInterceptChain;
import cn.tenbit.hare.core.util.HareObjectUtils;
import cn.tenbit.wolf.web.core.model.WebContextAndResult;
import cn.tenbit.wolf.web.core.util.WolfWebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author bangquan.qian
 * @Date 2019-06-19 11:40
 */
@Slf4j
public abstract class AbstractWolfWebController {

    @RequestMapping(value = "/{applicationKey}/{serviceKey}/{methodKey}", method = {RequestMethod.GET, RequestMethod.POST})
    public void service(HttpServletRequest request, HttpServletResponse response,
                        @PathVariable(value = "applicationKey") String applicationKey,
                        @PathVariable(value = "serviceKey") String serviceKey,
                        @PathVariable(value = "methodKey") String methodKey) {

        service(request, response, serviceKey, methodKey);
    }

    @RequestMapping(value = "/{serviceKey}/{methodKey}", method = {RequestMethod.GET, RequestMethod.POST})
    public void service(HttpServletRequest request, HttpServletResponse response,
                        @PathVariable(value = "serviceKey") String serviceKey,
                        @PathVariable(value = "methodKey") String methodKey) {
        try {
            handle(request, response, serviceKey, methodKey);
        } catch (Throwable e) {
            log.error("service", e);
        }
    }

    private void handle(HttpServletRequest request, HttpServletResponse response,
                        String serviceKey, String methodKey) throws Exception {

        if (needDirectHandle(request, response, serviceKey, methodKey)) {
            directHandle(request, response, serviceKey, methodKey);
            return;
        }

        WebContextAndResult wcar = new WebContextAndResult();
        try {
            wcar = initHandle(request, response, serviceKey, methodKey, wcar);
            wcar = MAIN_CHAIN.execute(wcar);
        } catch (Throwable e) {
            wcar = errorHandle(wcar, e);
        } finally {
            wcar = FINAL_CHAIN.execute(wcar);
        }
    }

    private final HareInterceptChain<WebContextAndResult> MAIN_CHAIN = HareInterceptChain.<WebContextAndResult>newBuilder()
            .then(this::beforeHandle)
            .then(this::doHandle)
            .then(this::afterHandle)
            .build();

    private final HareInterceptChain<WebContextAndResult> FINAL_CHAIN = HareInterceptChain.<WebContextAndResult>newBuilder()
            .then(this::lastHandle)
            .then(this::finalHandle)
            .then(this::printHandle)
            .build();

    private WebContextAndResult finalHandle(WebContextAndResult wcar) {
        return WolfWebUtils.finalHandle(wcar);
    }

    private WebContextAndResult doHandle(WebContextAndResult wcar) {
        return WolfWebUtils.doHandle(wcar);
    }

    private WebContextAndResult printHandle(WebContextAndResult wcar) {
        return WolfWebUtils.printHandle(wcar);
    }

    private WebContextAndResult errorHandle(WebContextAndResult wcar, Throwable e) {
        wcar = HareObjectUtils.newIfNull(wcar, WebContextAndResult.class);
        return WolfWebUtils.errorHandle(wcar, e);
    }

    private WebContextAndResult initHandle(HttpServletRequest request, HttpServletResponse response, String serviceKey, String methodKey, WebContextAndResult wcar) {
        wcar = HareObjectUtils.newIfNull(wcar, WebContextAndResult.class);
        return WolfWebUtils.initHandle(request, response, serviceKey, methodKey, wcar);
    }

    protected abstract boolean needDirectHandle(HttpServletRequest request, HttpServletResponse response, String serviceKey, String methodKey);

    protected abstract void directHandle(HttpServletRequest request, HttpServletResponse response, String serviceKey, String methodKey);

    protected abstract WebContextAndResult beforeHandle(WebContextAndResult context);

    protected abstract WebContextAndResult afterHandle(WebContextAndResult context);

    protected abstract WebContextAndResult lastHandle(WebContextAndResult context);

}

package cn.tenbit.wolf.web.core.controller;

import cn.tenbit.hare.core.exechain.HareExecuteChain;
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

    //-------------------------------------------------------------------------------------------------------------------------//

    private void handle(HttpServletRequest request, HttpServletResponse response,
                        String serviceKey, String methodKey) throws Exception {

        if (needDirectHandle(request, response, serviceKey, methodKey)) {
            directHandle(request, response, serviceKey, methodKey);
            return;
        }

        WebContextAndResult wcar = new WebContextAndResult();
        try {
            wcar = MAIN_CHAIN.execute(initHandle(request, response, serviceKey, methodKey, wcar));
        } catch (Throwable e) {
            wcar = ERROR_CHAIN.execute(errorHandle(wcar, e));
        } finally {
            wcar = FINAL_CHAIN.execute(wcar);
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------//

    private final HareInterceptChain<WebContextAndResult> MAIN_CHAIN = HareInterceptChain.<WebContextAndResult>newBuilder()
            .then(this::beforeDoHandle)
            .then(this::doHandle)
            .then(this::afterDoHandle)
            .build();

    private final HareInterceptChain<WebContextAndResult> ERROR_CHAIN = HareInterceptChain.<WebContextAndResult>newBuilder()
            .build();

    private final HareInterceptChain<WebContextAndResult> FINAL_CHAIN = HareInterceptChain.<WebContextAndResult>newBuilder()
            .then(this::beforeFinalHandle)
            .then(this::finalHandle)
            .then(this::afterFinalHandle)
            .build();

    //-------------------------------------------------------------------------------------------------------------------------//

    private final HareExecuteChain<WebContextAndResult> BEFORE_DO_CHAIN = HareExecuteChain.<WebContextAndResult>newBuilder()
            .then(this::)
            .then(this::beforeHandle)
            .build();

    private final HareExecuteChain<WebContextAndResult> AFTER_DO_CHAIN = HareExecuteChain.<WebContextAndResult>newBuilder()
            .then(this::afterHandle)
            .build();

    private final HareExecuteChain<WebContextAndResult> BEFORE_FINAL_CHAIN = HareExecuteChain.<WebContextAndResult>newBuilder()
            .then(this::lastHandle)
            .build();

    private final HareExecuteChain<WebContextAndResult> AFTER_FINAL_CHAIN = HareExecuteChain.<WebContextAndResult>newBuilder()
            .then(this::printHandle)
            .build();

    //-------------------------------------------------------------------------------------------------------------------------//

    private WebContextAndResult beforeDoHandle(WebContextAndResult wcar) throws Exception {
        return BEFORE_DO_CHAIN.execute(wcar);
    }

    private WebContextAndResult doHandle(WebContextAndResult wcar) {
        return WolfWebUtils.doHandle(wcar);
    }

    private WebContextAndResult afterDoHandle(WebContextAndResult wcar) throws Exception {
        return AFTER_DO_CHAIN.execute(wcar);
    }

    private WebContextAndResult beforeFinalHandle(WebContextAndResult wcar) throws Exception {
        return BEFORE_FINAL_CHAIN.execute(wcar);
    }

    private WebContextAndResult finalHandle(WebContextAndResult wcar) {
        return WolfWebUtils.finalHandle(wcar);
    }

    private WebContextAndResult afterFinalHandle(WebContextAndResult wcar) throws Exception {
        return AFTER_FINAL_CHAIN.execute(wcar);
    }

    //-------------------------------------------------------------------------------------------------------------------------//

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

    //-------------------------------------------------------------------------------------------------------------------------//

    protected abstract boolean needDirectHandle(HttpServletRequest request, HttpServletResponse response, String serviceKey, String methodKey);

    protected abstract void directHandle(HttpServletRequest request, HttpServletResponse response, String serviceKey, String methodKey);

    protected abstract void beforeHandle(WebContextAndResult context);

    protected abstract void afterHandle(WebContextAndResult context);

    protected abstract void lastHandle(WebContextAndResult context);

}

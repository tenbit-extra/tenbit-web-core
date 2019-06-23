package cn.tenbit.wolf.web.core.exechain;

import cn.tenbit.hare.core.exechain.HareExecutable;
import cn.tenbit.hare.core.util.HareAssertUtils;
import cn.tenbit.wolf.web.core.model.WebContextAndResult;

/**
 * @Author bangquan.qian
 * @Date 2019-06-21 17:52
 */
public class ApiRouteExecutor implements HareExecutable<WebContextAndResult> {

    @Override
    public void execute(WebContextAndResult target) throws Exception {
        HareAssertUtils.notNull(target);

        
    }
}

package cn.tenbit.wolf.web.core.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author bangquan.qian
 * @Date 2019-04-29 11:35
 */
@Component("wolfSpringBeans")
@Slf4j
public class WolfSpringBeans implements ApplicationContextAware {

    private static class Instance {
        private static final WolfSpringBeans INSTANCE = new WolfSpringBeans();
    }

    private ApplicationContext applicationContext;

    private DefaultListableBeanFactory beanDefinitionRegistry;

    public WolfSpringBeans() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.beanDefinitionRegistry = (DefaultListableBeanFactory) ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        getInstance().applicationContext = this.applicationContext;
        getInstance().beanDefinitionRegistry = this.beanDefinitionRegistry;
    }

    public static WolfSpringBeans getInstance() {
        return Instance.INSTANCE;
    }

    public static ApplicationContext getApplicationContext() {
        return getInstance().applicationContext;
    }

    public static DefaultListableBeanFactory getBeanDefinitionRegistry() {
        return getInstance().beanDefinitionRegistry;
    }

    public static void removeBeanDefinition(String beanName) {
        getBeanDefinitionRegistry().removeBeanDefinition(beanName);
    }

    public static boolean containsBeanDefinition(String beanName) {
        return getBeanDefinitionRegistry().containsBeanDefinition(beanName);
    }

    public static void registerBeanDefinitionIfNotExist(String beanName, String beanClassName) throws ClassNotFoundException {
        registerBeanDefinitionIfNotExist(beanName, ClassUtils.getClass(beanClassName));
    }

    public static void registerBeanDefinitionIfNotExist(String beanName, Class<?> beanClass) {
        if (containsBeanDefinition(beanName)) {
            return;
        }
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass.getCanonicalName());
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        getBeanDefinitionRegistry().registerBeanDefinition(beanName, beanDefinition);
    }

    public static Map<String, List<String>> getBeanDefinitionNames() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        ApplicationContext ctx = getApplicationContext();
        while (ctx != null) {
            map.put(ctx.getDisplayName(), Arrays.asList(ctx.getBeanDefinitionNames()));
            ctx = ctx.getParent();
        }
        return map;
    }

    public static boolean containsBean(String beanName) {
        return getApplicationContext().containsBean(beanName);
    }

    public static Object getBeanByName(String beanName) {
        return getApplicationContext().getBean(beanName);
    }

    public static <T> T getBeanByType(Class<T> clzName) {
        return getApplicationContext().getBean(clzName);
    }

    public static Object getBean(String name) {
        Object bean = null;
        try {
            bean = getBeanByName(name);
        } catch (Exception e) {
            log.error("getBeanByName", e);
        }
        if (bean != null) {
            return bean;
        }

        try {
            Class<?> clz = ClassUtils.getClass(name);
            bean = getBeanByType(clz);
        } catch (Exception e) {
            log.error("getBeanByType", e);
        }

        return bean;
    }

    public static Object getProxyTarget(Object proxyInstance) {
        if (proxyInstance == null) {
            throw new RuntimeException("beanInstance is null");
        }

        if (!AopUtils.isAopProxy(proxyInstance)) {
            return proxyInstance;
        }

        if (AopUtils.isCglibProxy(proxyInstance)) {
            try {
                Field h = proxyInstance.getClass().getDeclaredField("CGLIB$CALLBACK_0");
                h.setAccessible(true);

                Object dynamicAdvisedInterceptor = h.get(proxyInstance);
                Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
                advised.setAccessible(true);

                return ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }


}

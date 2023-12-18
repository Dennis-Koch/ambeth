//package com.koch.ambeth.ioc.cdi;
//
//import com.koch.ambeth.ioc.IExternalServiceContext;
//import com.koch.ambeth.ioc.config.IBeanConfiguration;
//import com.koch.ambeth.ioc.factory.BeanContextInit;
//import com.koch.ambeth.ioc.factory.BeanContextInitializer;
//import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
//import com.koch.ambeth.util.typeinfo.IPropertyInfo;
//import jakarta.enterprise.inject.spi.BeanManager;
//import lombok.NonNull;
//import lombok.RequiredArgsConstructor;
//
//import java.util.Set;
//
//@RequiredArgsConstructor
//public class CdiExternalServiceContext implements IExternalServiceContext {
//
//    @NonNull
//    protected final BeanManager beanManager;
//
//    @Override
//    public <T> T getServiceByType(Class<T> serviceType) {
//        var beans = beanManager.getBeans(serviceType);
//        if (!beans.isEmpty()) {
//            var firstBean = beans.iterator().next();
//            beanManager.getReference(firstBean, serviceType, );
//        }
//        var bundleContext = bundle.getBundleContext();
//        var serviceReference = bundleContext.getServiceReference(serviceType);
//        if (serviceReference == null) {
//            return null;
//        }
//        try {
//            return bundleContext.getService(serviceReference);
//        } catch (Throwable e) {
//            throw RuntimeExceptionUtil.mask(e, "Error occured while resolving OSGi component with '" + serviceReference + "'");
//        }
//    }
//
//    @Override
//    public boolean initializeAutowiring(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean, Class<?> beanType, IPropertyInfo[] propertyInfos,
//            Set<String> alreadySpecifiedPropertyNamesSet, Set<String> ignoredPropertyNamesSet, BeanContextInitializer beanContextInitializer, boolean highPriorityBean, IPropertyInfo prop) {
//        return false;
//    }
//}

package com.koch.ambeth.service.rest;

import java.lang.reflect.Method;
import java.net.URL;

public interface IRESTClientServiceUrlBuilder {
    URL buildURL(String serviceBaseUrl, String serviceName, Method method, Object[] args) throws Throwable;
}

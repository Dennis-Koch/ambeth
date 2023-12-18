package com.koch.ambeth.ioc;

public interface IServiceLookup {
    /**
     * Service bean lookup by name. Identical to getService(serviceName, true).
     *
     * @param serviceName Name of the service bean to lookup.
     * @return Requested service bean.
     */
    Object getService(String serviceName);

    /**
     * Service bean lookup by name that may return null.
     *
     * @param serviceName    Name of the service bean to lookup.
     * @param checkExistence Flag if bean is required to exist.
     * @return Requested service bean or null if bean does not exist and existence is not checked.
     */
    Object getService(String serviceName, boolean checkExistence);

    /**
     * Service bean lookup by name with defined return type.
     *
     * @param serviceName Name of the service bean to lookup.
     * @param targetType  Type the service bean is casted to.
     * @return Requested service bean.
     */
    <V> V getService(String serviceName, Class<V> targetType);

    /**
     * Service bean lookup by name with defined return type.
     *
     * @param serviceName    Name of the service bean to lookup.
     * @param targetType     Type the service bean is casted to.
     * @param checkExistence Flag if bean is required to exist.
     * @return Requested service bean or null if bean does not exist and existence is not checked.
     */
    <V> V getService(String serviceName, Class<V> targetType, boolean checkExistence);

    /**
     * Service bean lookup by type. Identical to getService(autowiredType, true)
     *
     * @param type Type the service bean is autowired to.
     * @return Requested service bean.
     */
    <T> T getService(Class<T> type);

    /**
     * Service bean lookup by type that may return null.
     *
     * @param type           Type the service bean is autowired to.
     * @param checkExistence Flag if bean is required to exist.
     * @return Requested service bean or null if bean does not exist and existence is not checked.
     */
    <T> T getService(Class<T> type, boolean checkExistence);
}

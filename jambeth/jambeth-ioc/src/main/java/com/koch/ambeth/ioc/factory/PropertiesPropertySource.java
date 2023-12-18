package com.koch.ambeth.ioc.factory;

import com.koch.ambeth.util.config.IProperties;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.lang.Nullable;

public class PropertiesPropertySource extends EnumerablePropertySource<IProperties> {

    /**
     * Create a new {@code MapPropertySource} with the given name and {@code Map}.
     *
     * @param name   the associated name
     * @param source the Map source (without {@code null} values in order to get
     *               consistent {@link #getProperty} and {@link #containsProperty} behavior)
     */
    public PropertiesPropertySource(String name, IProperties source) {
        super(name, source);
    }

    @Override
    @Nullable
    public Object getProperty(String name) {
        return source.get(name);
    }

    @Override
    public boolean containsProperty(String name) {
        return source.containsKey(name);
    }

    @Override
    public String[] getPropertyNames() {
        return source.collectAllPropertyKeys().toArray(String[]::new);
    }
}

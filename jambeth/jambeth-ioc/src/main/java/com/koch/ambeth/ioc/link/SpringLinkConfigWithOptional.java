package com.koch.ambeth.ioc.link;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpringLinkConfigWithOptional implements ILinkConfigWithOptional {
    @NonNull
    protected final LinkContainer link;

    @Override
    public ILinkConfigOptional with(Object... arguments) {
        link.setArguments(arguments);
        return this;
    }

    @Override
    public void optional() {
        link.setOptional(true);
    }
}

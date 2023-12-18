package com.koch.ambeth.xml.postprocess;

/*-
 * #%L
 * jambeth-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.util.collections.ArrayList;

import java.util.List;

public class TestXmlPostProcessor implements IXmlPostProcessor {
    public final List<String> handledTags = new ArrayList<>();

    @Override
    public Object processWrite(IPostProcessWriter writer) {
        return "";
    }

    @Override
    public void processRead(IPostProcessReader reader) {
        String elementName = reader.getElementName();
        handledTags.add(elementName);
    }
}

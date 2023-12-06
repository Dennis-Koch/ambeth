package com.koch.ambeth.xml.postprocess.merge;

/*-
 * #%L
 * jambeth-xml
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.merge.transfer.DirectObjRef;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.pending.ArraySetterCommand;
import com.koch.ambeth.xml.pending.IObjectCommand;
import com.koch.ambeth.xml.pending.IObjectFuture;

import java.lang.reflect.Array;

public class MergeArraySetterCommand extends ArraySetterCommand implements IObjectCommand, IInitializingBean {
    public MergeArraySetterCommand(IObjectFuture objectFuture, Object parent, int index) {
        super(objectFuture, parent, index);
    }

    @Override
    public void execute(IReader reader) {
        var value = objectFuture.getValue();
        if (IObjRef.class.isAssignableFrom(parent.getClass().getComponentType())) {
            // Happens in CUDResults in PostProcessing tags (<pp>)
            value = new DirectObjRef(value.getClass(), value);
        }
        Array.set(parent, index, value);
    }
}

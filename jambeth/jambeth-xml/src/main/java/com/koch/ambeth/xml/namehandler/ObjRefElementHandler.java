package com.koch.ambeth.xml.namehandler;

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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.xml.INameBasedHandler;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.IWriter;
import com.koch.ambeth.xml.typehandler.AbstractHandler;

public class ObjRefElementHandler extends AbstractHandler implements INameBasedHandler {
    protected static final String idNameIndex = "ix";
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;
    @Autowired
    protected IObjRefFactory objRefFactory;
    @LogInstance
    private ILogger log;

    @Override
    public boolean writesCustom(Object obj, Class<?> type, IWriter writer) {
        if (!IObjRef.class.isAssignableFrom(type) || IDirectObjRef.class.isAssignableFrom(type)) {
            return false;
        }
        IObjRef ori = (IObjRef) obj;
        writeOpenElement(ori, writer);
        writer.writeObject(ori.getRealType());
        writer.writeObject(ori.getId());
        writer.writeObject(ori.getVersion());
        writer.writeCloseElement(xmlDictionary.getEntityRefElement());
        return true;
    }

    @Override
    public Object readObject(Class<?> returnType, String elementName, int id, IReader reader) {
        if (!xmlDictionary.getEntityRefElement().equals(elementName)) {
            throw new IllegalStateException("Element '" + elementName + "' not supported");
        }

        String idIndexValue = reader.getAttributeValue(idNameIndex);
        byte idIndex = idIndexValue != null ? Byte.parseByte(idIndexValue) : ObjRef.PRIMARY_KEY_INDEX;
        reader.nextTag();
        Class<?> realType = (Class<?>) reader.readObject();
        Object objId = reader.readObject();
        Object version = reader.readObject();

        if (objId != null || version != null) {
            IEntityMetaData metaData = entityMetaDataProvider.getMetaData(realType, true);
            if (metaData != null) {
                if (objId != null) {
                    PrimitiveMember idMember = metaData.getIdMemberByIdIndex(idIndex);
                    if (objId.equals(idMember.getNullEquivalentValue())) {
                        objId = null;
                    }
                }
                if (version != null) {
                    PrimitiveMember versionMember = metaData.getVersionMember();
                    if (versionMember != null) {
                        if (version.equals(versionMember.getNullEquivalentValue())) {
                            version = null;
                        }
                    }
                }
            }
        }

        IObjRef obj = objRefFactory.createObjRef(realType, idIndex, objId, version);

        return obj;
    }

    protected void writeOpenElement(IObjRef ori, IWriter writer) {
        writer.writeStartElement(xmlDictionary.getEntityRefElement());
        int id = writer.acquireIdForObject(ori);
        writer.writeAttribute(xmlDictionary.getIdAttribute(), id);
        byte idIndex = ori.getIdNameIndex();
        if (idIndex != ObjRef.PRIMARY_KEY_INDEX) {
            writer.writeAttribute(idNameIndex, Byte.toString(ori.getIdNameIndex()));
        }
        writer.writeStartElementEnd();
    }
}

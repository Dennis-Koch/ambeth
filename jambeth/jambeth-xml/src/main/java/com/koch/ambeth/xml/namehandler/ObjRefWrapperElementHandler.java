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
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.xml.INameBasedHandler;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.IWriter;
import com.koch.ambeth.xml.pending.ObjRefFuture;
import com.koch.ambeth.xml.typehandler.AbstractHandler;

public class ObjRefWrapperElementHandler extends AbstractHandler implements INameBasedHandler {
    protected static final String idNameIndex = "ix";
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;
    @Autowired
    protected IObjRefFactory objRefFactory;
    @Autowired
    protected IObjRefHelper oriHelper;
    @LogInstance
    private ILogger log;

    @Override
    public boolean writesCustom(Object obj, Class<?> type, IWriter writer) {
        var metaData = entityMetaDataProvider.getMetaData(type, true);
        if (metaData == null) {
            return false;
        }

        var idValue = writer.getIdOfObject(obj);
        if (idValue != 0) {
            writer.writeStartElement(xmlDictionary.getRefElement());
            writer.writeAttribute(xmlDictionary.getIdAttribute(), idValue);
            writer.writeEndElement();
        } else {
            writer.addSubstitutedEntity(obj);
            var ori = oriHelper.entityToObjRef(obj, true);
            writeOpenElement(ori, obj, writer);
            writer.writeObject(ori.getRealType());
            writer.writeObject(ori.getId(), true);
            writer.writeObject(ori.getVersion(), true);
            writer.writeCloseElement(xmlDictionary.getOriWrapperElement());
        }

        return true;
    }

    @Override
    public Object readObject(Class<?> returnType, String elementName, int id, IReader reader) {
        if (!xmlDictionary.getOriWrapperElement().equals(elementName)) {
            throw new IllegalStateException("Element '" + elementName + "' not supported");
        }

        var idIndexValue = reader.getAttributeValue(idNameIndex);
        var idIndex = idIndexValue != null ? Byte.parseByte(idIndexValue) : ObjRef.PRIMARY_KEY_INDEX;
        reader.nextTag();
        var realType = (Class<?>) reader.readObject();
        var objId = reader.readObject();
        var version = reader.readObject();

        if (objId != null || version != null) {
            var metaData = entityMetaDataProvider.getMetaData(realType);
            if (metaData != null) {
                if (objId != null) {
                    var idMember = metaData.getIdMemberByIdIndex(idIndex);
                    if (objId.equals(idMember.getNullEquivalentValue())) {
                        objId = null;
                    }
                }
                if (version != null) {
                    var versionMember = metaData.getVersionMember();
                    if (versionMember != null) {
                        if (version.equals(versionMember.getNullEquivalentValue())) {
                            version = null;
                        }
                    }
                }
            }
        }

        var ori = objRefFactory.createObjRef(realType, idIndex, objId, version);

        var obj = new ObjRefFuture(ori);

        return obj;
    }

    protected void writeOpenElement(IObjRef ori, Object obj, IWriter writer) {
        writer.writeStartElement(xmlDictionary.getOriWrapperElement());
        var id = writer.acquireIdForObject(obj);
        writer.writeAttribute(xmlDictionary.getIdAttribute(), id);
        var idIndex = ori.getIdNameIndex();
        if (idIndex != ObjRef.PRIMARY_KEY_INDEX) {
            writer.writeAttribute(idNameIndex, Byte.toString(ori.getIdNameIndex()));
        }
        writer.writeStartElementEnd();
    }
}

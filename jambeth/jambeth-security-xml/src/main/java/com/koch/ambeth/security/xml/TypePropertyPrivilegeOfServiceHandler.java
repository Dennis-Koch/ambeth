package com.koch.ambeth.security.xml;

import com.koch.ambeth.security.privilege.transfer.TypePropertyPrivilegeOfService;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.ITypeBasedHandler;
import com.koch.ambeth.xml.IWriter;
import com.koch.ambeth.xml.typehandler.AbstractHandler;

public class TypePropertyPrivilegeOfServiceHandler extends AbstractHandler implements ITypeBasedHandler {

    @Override
    public Object readObject(Class<?> returnType, Class<?> objType, int id, IReader reader) {
        return TypePropertyPrivilegeOfService.createFromSerialized(reader.getAttributeValue(xmlDictionary.getValueAttribute()));
    }

    @Override
    public void writeObject(Object obj, Class<?> type, IWriter writer, boolean suppressReference) {
        TypePropertyPrivilegeOfService tppos = (TypePropertyPrivilegeOfService) obj;
        writer.writeAttribute(xmlDictionary.getValueAttribute(), tppos.toSerializedString());
    }
}

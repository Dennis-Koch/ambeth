package com.koch.ambeth.xml.typehandler;

import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.ITypeBasedHandler;
import com.koch.ambeth.xml.IWriter;

import java.util.regex.Pattern;

public class PatternTypeHandler extends AbstractHandler implements ITypeBasedHandler {
    @Override
    public void writeObject(Object obj, Class<?> type, IWriter writer, boolean suppressReference) {
        writer.writeAttribute(xmlDictionary.getValueAttribute(), ((Pattern) obj).pattern());
    }

    @Override
    public Object readObject(Class<?> returnType, Class<?> objType, int id, IReader reader) {
        return Pattern.compile(reader.getAttributeValue(xmlDictionary.getValueAttribute()));
    }
}

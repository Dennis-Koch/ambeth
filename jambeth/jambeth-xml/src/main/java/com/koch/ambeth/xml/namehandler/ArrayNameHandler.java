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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.Arrays;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.converter.EncodingInformation;
import com.koch.ambeth.xml.INameBasedHandler;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.IWriter;
import com.koch.ambeth.xml.pending.ICommandBuilder;
import com.koch.ambeth.xml.pending.IObjectFuture;
import com.koch.ambeth.xml.typehandler.AbstractHandler;

import java.lang.reflect.Array;
import java.util.regex.Pattern;

public class ArrayNameHandler extends AbstractHandler implements INameBasedHandler, IInitializingBean {
    public static final String primitiveValueSeparator = ";";

    protected static final Pattern splitPattern = Pattern.compile(primitiveValueSeparator);

    protected ICommandBuilder commandBuilder;

    @Override
    public void afterPropertiesSet() throws Throwable {
        super.afterPropertiesSet();

        ParamChecker.assertNotNull(commandBuilder, "commandBuilder");
    }

    public void setCommandBuilder(ICommandBuilder commandBuilder) {
        this.commandBuilder = commandBuilder;
    }

    @Override
    public boolean writesCustom(Object obj, Class<?> type, IWriter writer) {
        if (!type.isArray()) {
            return false;
        }
        var conversionHelper = this.conversionHelper;
        var xmlDictionary = this.xmlDictionary;
        var arrayElement = xmlDictionary.getArrayElement();
        writer.writeStartElement(arrayElement);
        var id = writer.acquireIdForObject(obj);
        writer.writeAttribute(xmlDictionary.getIdAttribute(), id);
        var length = Array.getLength(obj);
        writer.writeAttribute(xmlDictionary.getSizeAttribute(), length);
        var componentType = type.getComponentType();
        classElementHandler.writeAsAttribute(componentType, writer);
        if (length == 0) {
            writer.writeEndElement();
        } else {
            writer.writeStartElementEnd();
            if (componentType.isPrimitive()) {
                writer.write("<values v=\"");
                if (char.class.equals(componentType) || byte.class.equals(componentType) || boolean.class.equals(componentType)) {
                    var value = conversionHelper.convertValueToType(String.class, obj, EncodingInformation.SOURCE_PLAIN | EncodingInformation.TARGET_BASE64);
                    writer.write(value);
                } else {
                    var preparedArrayGet = Arrays.prepareGet(obj);
                    for (int a = 0; a < length; a++) {
                        var item = preparedArrayGet.get(a);
                        if (a > 0) {
                            writer.write(primitiveValueSeparator);
                        }
                        var value = conversionHelper.convertValueToType(String.class, item);
                        writer.writeEscapedXml(value);
                    }
                }
                writer.write("\"/>");
            } else {
                var preparedArrayGet = Arrays.prepareGet(obj);
                for (int a = 0; a < length; a++) {
                    var item = preparedArrayGet.get(a);
                    writer.writeObject(item);
                }
            }
            writer.writeCloseElement(arrayElement);
        }
        return true;
    }

    @Override
    public Object readObject(Class<?> returnType, String elementName, int id, IReader reader) {
        var conversionHelper = this.conversionHelper;
        var xmlDictionary = this.xmlDictionary;
        if (!xmlDictionary.getArrayElement().equals(elementName)) {
            throw new IllegalStateException("Element '" + elementName + "' not supported");
        }
        int length = Integer.parseInt(reader.getAttributeValue(xmlDictionary.getSizeAttribute()));
        var componentType = classElementHandler.readFromAttribute(reader);

        Object targetArray;
        if (!reader.isEmptyElement()) {
            reader.nextTag();
        }
        if ("values".equals(reader.getElementName())) {
            var listOfValuesString = reader.getAttributeValue("v");
            if (char.class.equals(componentType) || byte.class.equals(componentType) || boolean.class.equals(componentType)) {
                targetArray = Array.newInstance(componentType, 0);
                targetArray = conversionHelper.convertValueToType(targetArray.getClass(), listOfValuesString, EncodingInformation.SOURCE_BASE64 | EncodingInformation.TARGET_PLAIN);
                if (id > 0) {
                    reader.putObjectWithId(targetArray, id);
                }
            } else {
                targetArray = Array.newInstance(componentType, length);
                if (id > 0) {
                    reader.putObjectWithId(targetArray, id);
                }
                var items = splitPattern.split(listOfValuesString);
                var preparedConverter = conversionHelper.prepareConverter(componentType);
                var preparedArraySet = Arrays.prepareSet(targetArray);
                for (int a = 0, size = items.length; a < size; a++) {
                    var item = items[a];
                    if (item == null || item.length() == 0) {
                        continue;
                    }
                    var convertedValue = preparedConverter.convertValue(items[a], null);
                    preparedArraySet.set(a, convertedValue);
                }
            }
            reader.moveOverElementEnd();
        } else {
            targetArray = Array.newInstance(componentType, length);
            var preparedArraySet = Arrays.prepareSet(targetArray);

            reader.putObjectWithId(targetArray, id);
            var commandTypeRegistry = reader.getCommandTypeRegistry();
            var commandBuilder = this.commandBuilder;
            for (int index = 0; index < length; index++) {
                var item = reader.readObject();
                if (item instanceof IObjectFuture) {
                    var objectFuture = (IObjectFuture) item;
                    var command = commandBuilder.build(commandTypeRegistry, objectFuture, targetArray, index);
                    reader.addObjectCommand(command);
                } else {
                    preparedArraySet.set(index, item);
                }
            }
        }
        return targetArray;
    }
}

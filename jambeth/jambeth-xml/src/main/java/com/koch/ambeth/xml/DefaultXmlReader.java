package com.koch.ambeth.xml;

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

import com.koch.ambeth.ioc.extendable.IMapExtendableContainer;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IntKeyMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.xml.pending.ICommandCreator;
import com.koch.ambeth.xml.pending.ICommandTypeExtendable;
import com.koch.ambeth.xml.pending.ICommandTypeRegistry;
import com.koch.ambeth.xml.pending.IObjectCommand;
import com.koch.ambeth.xml.pending.IObjectFuture;
import com.koch.ambeth.xml.pending.IObjectFutureHandlerRegistry;
import com.koch.ambeth.xml.pending.MergeCommand;
import com.koch.ambeth.xml.postprocess.IPostProcessReader;
import lombok.SneakyThrows;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.EOFException;
import java.util.List;

public class DefaultXmlReader implements IReader, IPostProcessReader, ICommandTypeRegistry, ICommandTypeExtendable {
    protected final IntKeyMap<Object> idToObjectMap = new IntKeyMap<>();

    protected final HashMap<Class<?>, SpecifiedMember[]> typeToMemberMap = new HashMap<>();

    protected final List<IObjectCommand> objectCommands = new ArrayList<>();

    protected final IMapExtendableContainer<Class<? extends IObjectCommand>, ICommandCreator> commandTypeExtendable = new MapExtendableContainer<>("Overriding command type", "Original command type");

    protected final XmlPullParser pullParser;

    protected final ICyclicXmlController xmlController;

    protected final IObjectFutureHandlerRegistry objectFutureHandlerRegistry;

    public DefaultXmlReader(XmlPullParser pullParser, ICyclicXmlController xmlController, IObjectFutureHandlerRegistry objectFutureHandlerRegistry) {
        this.pullParser = pullParser;
        this.xmlController = xmlController;
        this.objectFutureHandlerRegistry = objectFutureHandlerRegistry;
    }

    @Override
    public String getAttributeValue(String attributeName) {
        return pullParser.getAttributeValue(null, attributeName);
    }

    @Override
    public Object readObject() {
        var object = xmlController.readObject(this);
        return object;
    }

    @Override
    public Object readObject(Class<?> returnType) {
        var object = xmlController.readObject(returnType, this);
        return object;
    }

    @Override
    public String getElementName() {
        return pullParser.getName();
    }

    @SneakyThrows
    @Override
    public boolean nextToken() {
        try {
            pullParser.nextToken();
            while (pullParser.getEventType() == XmlPullParser.TEXT && pullParser.isWhitespace()) {
                pullParser.nextToken();
            }
            return true;
        } catch (EOFException e) {
            return false;
        }
    }

    @SneakyThrows
    @Override
    public String getElementValue() {
        return pullParser.getText();
    }

    @SneakyThrows
    @Override
    public boolean isEmptyElement() {
        return pullParser.isEmptyElementTag();
    }

    @SneakyThrows
    @Override
    public void moveOverElementEnd() {
        try {
            if (pullParser.getEventType() == XmlPullParser.END_TAG) {
                pullParser.nextTag();
            } else if (pullParser.isEmptyElementTag()) {
                pullParser.nextTag();
                pullParser.nextTag();
            }
        } catch (XmlPullParserException e) {
            if (e.getMessage().startsWith("expected START_TAG or END_TAG not END_DOCUMENT")) {
                return;
            }
        }
    }

    @Override
    public boolean nextTag() {
        try {
            pullParser.nextTag();
            return true;
        } catch (EOFException e) {
            return false;
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @Override
    public boolean isStartTag() {
        try {
            return pullParser.getEventType() == XmlPullParser.START_TAG;
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @Override
    public boolean isEndTag() {
        try {
            return pullParser.getEventType() == XmlPullParser.END_TAG;
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @Override
    public Object getObjectById(int id) {
        return getObjectById(id, true);
    }

    @Override
    public Object getObjectById(int id, boolean checkExistence) {
        var object = idToObjectMap.get(id);
        if (object == null && checkExistence && !idToObjectMap.containsKey(id)) {
            throw new IllegalStateException("No object found in xml with id " + id);
        }
        return object;
    }

    @Override
    public void putObjectWithId(Object obj, int id) {
        if (idToObjectMap.putIfNotExists(id, obj)) {
            return;
        }
        var existingObj = idToObjectMap.get(id);
        if (existingObj == obj) {
            return;
        }
        throw new IllegalStateException("Already mapped object to id " + id + " found");
    }

    @Override
    public void putMembersOfType(Class<?> type, SpecifiedMember[] members) {
        if (!typeToMemberMap.putIfNotExists(type, members)) {
            throw new IllegalStateException("Already mapped type '" + type + "'");
        }
    }

    @Override
    public SpecifiedMember[] getMembersOfType(Class<?> type) {
        return typeToMemberMap.get(type);
    }

    @Override
    public void addObjectCommand(IObjectCommand objectCommand) {
        objectCommands.add(objectCommand);
    }

    @Override
    public void executeObjectCommands() {
        while (!objectCommands.isEmpty()) {
            var commandSnapShot = new ArrayList<>(objectCommands);
            objectCommands.clear();

            resolveObjectFutures(commandSnapShot);

            // Commands have to be executed in-order (e.g. for CollectionSetterCommands)
            // except for MergeCommand which have to be last
            var mergeCommands = new ArrayList<IObjectCommand>(commandSnapShot.size());
            for (int i = 0, size = commandSnapShot.size(); i < size; i++) {
                var objectCommand = commandSnapShot.get(i);
                if (objectCommand instanceof MergeCommand) {
                    mergeCommands.add(objectCommand);
                    continue;
                }
                objectCommand.execute(this);
            }
            for (int i = 0, size = mergeCommands.size(); i < size; i++) {
                var objectCommand = mergeCommands.get(i);
                objectCommand.execute(this);
            }
        }
    }

    protected void resolveObjectFutures(List<IObjectCommand> objectCommands) {
        var objectFutureHandlerRegistry = this.objectFutureHandlerRegistry;
        var sortedObjectFutures = bucketSortObjectFutures(objectCommands);
        for (var entry : sortedObjectFutures) {
            var type = entry.getKey();
            var objectFutures = entry.getValue();
            var objectFutureHandler = objectFutureHandlerRegistry.getObjectFutureHandler(type);
            if (objectFutureHandler == null) {
                throw new UnsupportedOperationException("No handler found for " + IObjectFuture.class.getSimpleName() + "s of type '" + type.getName() + "'");
            }
            objectFutureHandler.handle(objectFutures.toList());
        }
    }

    protected ILinkedMap<Class<? extends IObjectFuture>, ISet<IObjectFuture>> bucketSortObjectFutures(List<IObjectCommand> objectCommands) {
        var sortedObjectFutures = LinkedHashMap.<Class<? extends IObjectFuture>, ISet<IObjectFuture>>create(objectCommands.size());
        for (int i = 0, size = objectCommands.size(); i < size; i++) {
            var objectCommand = objectCommands.get(i);
            var objectFuture = objectCommand.getObjectFuture();
            if (objectFuture != null) {
                var type = objectFuture.getClass();
                var objectFutures = sortedObjectFutures.get(type);
                if (objectFutures == null) {
                    objectFutures = new LinkedHashSet<>();
                    sortedObjectFutures.put(type, objectFutures);
                }
                objectFutures.add(objectFuture);
            }
        }
        return sortedObjectFutures;
    }

    @Override
    public ICommandTypeRegistry getCommandTypeRegistry() {
        return this;
    }

    @Override
    public ICommandTypeExtendable getCommandTypeExtendable() {
        return this;
    }

    @Override
    public ICommandCreator getOverridingCommandType(Class<? extends IObjectCommand> commandType) {
        return commandTypeExtendable.getExtension(commandType);
    }

    @Override
    public void registerOverridingCommandCreator(ICommandCreator overridingCommandCreator, Class<? extends IObjectCommand> commandType) {
        commandTypeExtendable.register(overridingCommandCreator, commandType);
    }

    @Override
    public void unregisterOverridingCommandCreator(ICommandCreator overridingCommandCreator, Class<? extends IObjectCommand> commandType) {
        commandTypeExtendable.unregister(overridingCommandCreator, commandType);
    }

    @Override
    public String toString() {
        if (isStartTag()) {
            if (isEmptyElement()) {
                return getElementName() + "|EMPTY";
            }
            return getElementName() + "|START";
        }
        return getElementName() + "|END";
    }
}

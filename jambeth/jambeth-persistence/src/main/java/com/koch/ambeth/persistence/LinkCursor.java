package com.koch.ambeth.persistence;

import com.koch.ambeth.persistence.api.ILinkCursor;
import com.koch.ambeth.persistence.api.ILinkCursorItem;

import java.util.Iterator;
import java.util.List;

public class LinkCursor implements ILinkCursor, ILinkCursorItem, Iterator<ILinkCursorItem> {
    protected List<? extends ILinkCursorItem> items;
    protected int currentIndex;

    protected byte fromIdIndex, toIdIndex;

    @Override
    public byte getFromIdIndex() {
        return fromIdIndex;
    }

    public void setFromIdIndex(byte fromIdIndex) {
        this.fromIdIndex = fromIdIndex;
    }

    @Override
    public byte getToIdIndex() {
        return toIdIndex;
    }

    public void setToIdIndex(byte toIdIndex) {
        this.toIdIndex = toIdIndex;
    }

    public void setItems(List<? extends ILinkCursorItem> items) {
        this.items = items;
    }

    @Override
    public boolean hasNext() {
        return items.size() > currentIndex;
    }

    @Override
    public ILinkCursorItem next() {
        currentIndex++;
        return items.get(currentIndex);
    }

    @Override
    public void dispose() {
        items = null;
    }

    @Override
    public Object getFromId() {
        return items.get(currentIndex).getFromId();
    }

    @Override
    public Object getToId() {
        return items.get(currentIndex).getToId();
    }

    @Override
    public Iterator<ILinkCursorItem> iterator() {
        return this;
    }
}

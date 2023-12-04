package com.koch.ambeth.merge.server.change;

/*-
 * #%L
 * jambeth-merge-server
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

import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.IList;

import java.util.List;

public class LinkChangeCommand extends AbstractChangeCommand implements ILinkChangeCommand {
    protected final IDirectedLink link;
    protected final byte toIdIndex;
    protected IList<IObjRef> refsToLink = EmptyList.<IObjRef>getInstance();
    protected IList<IObjRef> refsToUnlink = EmptyList.<IObjRef>getInstance();

    public LinkChangeCommand(IObjRef reference, IDirectedLink link) {
        super(reference);
        this.link = link;
        var fromIdIndex = getDirectedLink().getMetaData().getFromIdIndex();
        if (!(reference instanceof IDirectObjRef) && fromIdIndex != reference.getIdNameIndex()) {
            throw new IllegalStateException();
        }
        toIdIndex = getDirectedLink().getMetaData().getToIdIndex();
    }

    @Override
    public IDirectedLink getDirectedLink() {
        return link;
    }

    @Override
    public List<IObjRef> getRefsToLink() {
        return refsToLink;
    }

    @Override
    public List<IObjRef> getRefsToUnlink() {
        return refsToUnlink;
    }

    @Override
    public IChangeCommand addCommand(IChangeCommand other) {
        if (!(other instanceof ILinkChangeCommand)) {
            throw new IllegalCommandException("Cannot add create/update/change command to a link command!");
        }
        var linkChangeCommand = (ILinkChangeCommand) other;
        addRefsToLink(linkChangeCommand.getRefsToLink());
        addRefsToUnlink(linkChangeCommand.getRefsToUnlink());
        return null;
    }

    @Override
    protected IChangeCommand addCommand(ICreateCommand other) {
        throw new IllegalCommandException("Cannot add create command to a link command!");
    }

    @Override
    protected IChangeCommand addCommand(IUpdateCommand other) {
        throw new IllegalCommandException("Cannot add update command to a link command!");
    }

    @Override
    protected IChangeCommand addCommand(IDeleteCommand other) {
        throw new IllegalCommandException("Cannot add delete command to a link command!");
    }

    @Override
    public boolean isReadyToExecute() {
        var refs = refsToLink;
        for (int i = refs.size(); i-- > 0; ) {
            if (refs.get(i).getId() == null) {
                return false;
            }
        }
        refs = refsToUnlink;
        for (int i = refs.size(); i-- > 0; ) {
            if (refs.get(i).getId() == null) {
                return false;
            }
        }
        return true;
    }

    protected void checkCorrectIdIndex(IObjRef objRef) {
        // if (objRef instanceof IDirectObjRef && ((IDirectObjRef) objRef).getDirect() != null)
        // {
        // return;
        // }
        // if (objRef.getIdNameIndex() != toIdIndex)
        // {
        // System.out.println("AAAAAAAAAAAAAAAA");
        // // throw new IllegalStateException();
        // }
    }

    public void addRefsToLink(IObjRef[] addedORIs) {
        if (addedORIs.length == 0) {
            return;
        }
        var refsToLink = this.refsToLink;
        if (refsToLink.isEmpty()) {
            refsToLink = new ArrayList<>();
            this.refsToLink = refsToLink;
        }
        for (var addedORI : addedORIs) {
            checkCorrectIdIndex(addedORI);
            refsToLink.add(addedORI);
        }
        // refsToLink.addAll(addedORIs);
    }

    public void addRefsToLink(List<IObjRef> addedORIs) {
        if (addedORIs.isEmpty()) {
            return;
        }
        var refsToLink = this.refsToLink;
        if (refsToLink.isEmpty()) {
            refsToLink = new ArrayList<>();
            this.refsToLink = refsToLink;
        }
        for (int a = 0, size = addedORIs.size(); a < size; a++) {
            var addedObjRef = addedORIs.get(a);
            checkCorrectIdIndex(addedObjRef);
            refsToLink.add(addedObjRef);
        }
        // refsToLink.addAll(addedORIs);
    }

    public void addRefToLink(IObjRef addedObjRef) {
        var refsToLink = this.refsToLink;
        if (refsToLink.isEmpty()) {
            refsToLink = new ArrayList<>();
            this.refsToLink = refsToLink;
        }
        checkCorrectIdIndex(addedObjRef);
        refsToLink.add(addedObjRef);
    }

    public void addRefsToUnlink(IObjRef[] removedORIs) {
        if (removedORIs.length == 0) {
            return;
        }
        var refsToUnlink = this.refsToUnlink;
        if (refsToUnlink.isEmpty()) {
            refsToUnlink = new ArrayList<>();
            this.refsToUnlink = refsToUnlink;
        }
        for (var removedORI : removedORIs) {
            checkCorrectIdIndex(removedORI);
            refsToUnlink.add(removedORI);
        }
        // refsToUnlink.addAll(removedORIs);
    }

    public void addRefsToUnlink(List<IObjRef> removedORIs) {
        if (removedORIs.isEmpty()) {
            return;
        }
        var refsToUnlink = this.refsToUnlink;
        if (refsToUnlink.isEmpty()) {
            refsToUnlink = new ArrayList<>();
            this.refsToUnlink = refsToUnlink;
        }
        for (int a = 0, size = removedORIs.size(); a < size; a++) {
            var removedORI = removedORIs.get(a);
            checkCorrectIdIndex(removedORI);
            refsToUnlink.add(removedORI);
        }
        // refsToUnlink.addAll(removedORIs);
    }

    public void addRefToUnlink(IObjRef removedObjRef) {
        var refsToUnlink = this.refsToUnlink;
        if (refsToUnlink.isEmpty()) {
            refsToUnlink = new ArrayList<>();
            this.refsToUnlink = refsToUnlink;
        }
        checkCorrectIdIndex(removedObjRef);
        refsToUnlink.add(removedObjRef);
    }
}

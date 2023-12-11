package com.koch.ambeth.merge;

/*-
 * #%L
 * jambeth-merge
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

public interface IRevertChangesHelper {
    IRevertChangesSavepoint createSavepoint(Object source);

    IRevertChangesSavepoint createSavepoint(Object... sources);

    void revertChanges(Object objectsToRevert);

    void revertChanges(Object objectsToRevert, boolean recursive);

    void revertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback);

    void revertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive, boolean dirtyOnly);

    void revertChangesGlobally(Object objectsToRevert);

    void revertChangesGlobally(Object objectsToRevert, boolean recursive);

    void revertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback);

    void revertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive, boolean dirtyOnly);
}

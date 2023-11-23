package com.koch.ambeth.persistence.jdbc.mapping.models;

/*-
 * #%L
 * jambeth-test
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

import jakarta.persistence.Embeddable;

import com.koch.ambeth.model.AbstractEntity;

@Embeddable
public class TestEmbeddedTypeVO extends AbstractEntity {
    protected String name;

    protected int value;

    public String getNameString() {
        return name;
    }

    public void setNameString(String name) {
        this.name = name;
    }

    public int getValueNumber() {
        return value;
    }

    public void setValueNumber(int value) {
        this.value = value;
    }
}

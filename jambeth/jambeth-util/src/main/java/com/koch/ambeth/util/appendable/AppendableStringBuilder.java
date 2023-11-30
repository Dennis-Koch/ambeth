package com.koch.ambeth.util.appendable;

/*-
 * #%L
 * jambeth-util
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

public class AppendableStringBuilder implements IAppendable, CharSequence {
    protected final StringBuilder sb;

    public AppendableStringBuilder() {
        this(new StringBuilder());
    }

    public AppendableStringBuilder(StringBuilder sb) {
        this.sb = sb;
    }

    public void reset() {
        sb.setLength(0);
    }

    @Override
    public IAppendable append(char value) {
        sb.append(value);
        return this;
    }

    @Override
    public IAppendable append(int value) {
        sb.append(value);
        return this;
    }

    @Override
    public IAppendable append(CharSequence value) {
        sb.append(value);
        return this;
    }

    @Override
    public char charAt(int index) {
        return sb.charAt(index);
    }

    @Override
    public int length() {
        return sb.length();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}

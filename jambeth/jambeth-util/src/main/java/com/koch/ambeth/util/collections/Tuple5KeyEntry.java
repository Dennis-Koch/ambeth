package com.koch.ambeth.util.collections;

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

public class Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> {
	private final Key1 key1;

	private final Key2 key2;

	private final Key3 key3;

	private final Key4 key4;

	private final Key5 key5;

	private Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> nextEntry;

	private final int hash;

	private V value;

	public Tuple5KeyEntry(Key1 key1, Key2 key2, Key3 key3, Key4 key4, Key5 key5, V value, int hash,
			Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> nextEntry) {
		this.key1 = key1;
		this.key2 = key2;
		this.key3 = key3;
		this.key4 = key4;
		this.key5 = key5;
		this.value = value;
		this.hash = hash;
		this.nextEntry = nextEntry;
	}

	public int getHash() {
		return hash;
	}

	public Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> getNextEntry() {
		return nextEntry;
	}

	public void setNextEntry(Tuple5KeyEntry<Key1, Key2, Key3, Key4, Key5, V> nextEntry) {
		this.nextEntry = nextEntry;
	}

	public Key1 getKey1() {
		return key1;
	}

	public Key2 getKey2() {
		return key2;
	}

	public Key3 getKey3() {
		return key3;
	}

	public Key4 getKey4() {
		return key4;
	}

	public Key5 getKey5() {
		return key5;
	}

	public void setValue(V value) {
		this.value = value;
	}

	public V getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "(" + getKey1() + ":" + getKey2() + ":" + getKey3() + ":" + getKey4() + ":" + getKey5()
				+ "," + getValue();
	}
}

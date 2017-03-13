package com.koch.ambeth.ioc.util;

public class Strong2Key<V> {
	protected final V extension;

	protected final ConversionKey key;

	public Strong2Key(V extension, ConversionKey key) {
		this.extension = extension;
		this.key = key;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Strong2Key)) {
			return false;
		}
		Strong2Key<?> other = (Strong2Key<?>) obj;
		return extension == other.extension && key.equals(other.key);
	}

	public ConversionKey getKey() {
		return key;
	}

	public V getExtension() {
		return extension;
	}

	@Override
	public int hashCode() {
		return extension.hashCode() ^ key.hashCode();
	}
}

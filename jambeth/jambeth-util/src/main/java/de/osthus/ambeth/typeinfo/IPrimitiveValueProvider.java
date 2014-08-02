package de.osthus.ambeth.typeinfo;

public interface IPrimitiveValueProvider
{
	boolean getBoolean(int index);

	char getChar(int index);

	byte getByte(int index);

	float getFloat(int index);

	double getDouble(int index);

	short getShort(int index);

	int getInt(int index);

	long getLong(int index);

	Object getBoxedValue(int index);
}

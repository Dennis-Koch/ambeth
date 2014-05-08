package de.osthus.ambeth.persistence;

public interface IPositionedField extends IField
{

	Object getValue();

	void getValue(Object value);

}

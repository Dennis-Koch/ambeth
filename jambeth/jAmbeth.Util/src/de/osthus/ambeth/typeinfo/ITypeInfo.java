package de.osthus.ambeth.typeinfo;

public interface ITypeInfo
{
	String getSimpleName();

	Class<?> getRealType();

	ITypeInfoItem[] getMembers();

	ITypeInfoItem getMemberByName(String memberName);

	ITypeInfoItem getMemberByXmlName(String xmlMemberName);

	boolean doesImplement(Class<?> interfaceArgument);

	@Override
	String toString();
}

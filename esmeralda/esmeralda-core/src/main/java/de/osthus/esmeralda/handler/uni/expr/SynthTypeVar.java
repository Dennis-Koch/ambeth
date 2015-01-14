package de.osthus.esmeralda.handler.uni.expr;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

public class SynthTypeVar implements TypeMirror
{
	protected final String typeName;

	public SynthTypeVar(String typeName)
	{
		this.typeName = typeName;
	}

	@Override
	public TypeKind getKind()
	{
		return TypeKind.TYPEVAR;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString()
	{
		return typeName;
	}
}

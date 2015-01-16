package de.osthus.esmeralda.handler.uni.expr;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.EmptySet;

public class MockVariableElement implements VariableElement
{
	protected final Name variableName;

	protected final TypeMirror typeMirror;

	public MockVariableElement(String variableName, String typeName)
	{
		this.variableName = new MockName(variableName);
		typeMirror = new SynthTypeVar(typeName);
	}

	@Override
	public Name getSimpleName()
	{
		return variableName;
	}

	@Override
	public Set<Modifier> getModifiers()
	{
		return EmptySet.emptySet();
	}

	@Override
	public ElementKind getKind()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Element getEnclosingElement()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<? extends Element> getEnclosedElements()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors()
	{
		return EmptyList.getInstance();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType)
	{
		return null;
	}

	@Override
	public TypeMirror asType()
	{
		return typeMirror;
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getConstantValue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString()
	{
		return getSimpleName().toString();
	}
}

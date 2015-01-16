package de.osthus.esmeralda.handler.uni.expr;

import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class SynthVariableElement implements VariableElement
{
	protected final VariableElement baseVariableElement;

	protected final TypeMirror typeMirror;

	public SynthVariableElement(VariableElement baseVariableElement, String typeName)
	{
		this.baseVariableElement = baseVariableElement;
		typeMirror = new SynthTypeVar(typeName);
	}

	@Override
	public Name getSimpleName()
	{
		return baseVariableElement.getSimpleName();
	}

	@Override
	public Set<Modifier> getModifiers()
	{
		return baseVariableElement.getModifiers();
	}

	@Override
	public ElementKind getKind()
	{
		return baseVariableElement.getKind();
	}

	@Override
	public Element getEnclosingElement()
	{
		return baseVariableElement.getEnclosingElement();
	}

	@Override
	public java.util.List<? extends Element> getEnclosedElements()
	{
		return baseVariableElement.getEnclosedElements();
	}

	@Override
	public java.util.List<? extends AnnotationMirror> getAnnotationMirrors()
	{
		return baseVariableElement.getAnnotationMirrors();
	}

	@Override
	public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> annotationType)
	{
		return baseVariableElement.getAnnotation(annotationType);
	}

	@Override
	public TypeMirror asType()
	{
		return typeMirror;
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return baseVariableElement.accept(v, p);
	}

	@Override
	public Object getConstantValue()
	{
		return baseVariableElement.getConstantValue();
	}

	@Override
	public String toString()
	{
		return getSimpleName().toString();
	}
}

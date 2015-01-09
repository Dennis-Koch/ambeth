package de.osthus.ambeth.expr;

public interface IEntityPropertyExpressionResolver
{
	Object resolveExpressionOnEntity(Object entity, String expression);
}

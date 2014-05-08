package de.osthus.ambeth.ioc.config;

/**
 * Use this value together with a bean configuration This will stall the initialization of this bean till all other beans (with their default precedence) will
 * be initialized. The initialization order of beans with the same precedence still remains undefined
 */
public enum PrecedenceType
{
	DEFAULT, LOWEST, LOWER, LOW, MEDIUM, HIGH, HIGHER, HIGHEST;
}

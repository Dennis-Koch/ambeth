package de.osthus.ambeth.security;

import java.util.regex.Pattern;

public interface IUseCase
{
	Pattern[] getPatterns();

	UsecaseApplyType getApplyType();
}

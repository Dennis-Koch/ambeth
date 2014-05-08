package de.osthus.ambeth.util;

/**
 * Interface for the Ambeth conversion feature. It is used throughout the framework to convert values to different types in one unified way. Event the most
 * basic Ambeth context has a bean autowired to this interface. The conversion feature is extendible via the {@wiki wikipedia_en Extensibility_pattern
 * Extensibility pattern} by implementing the {@link IDedicatedConverter} interface an linking the bean to {@link IDedicatedConverterExtendable}.
 */
public interface IConversionHelper
{
	/**
	 * Primary method to convert values.
	 * 
	 * @param expectedType
	 *            Conversion target type.
	 * @param value
	 *            Value to be converted.
	 * @return Representation of the given value as the target type.
	 */
	<T> T convertValueToType(Class<T> expectedType, Object value);

	/**
	 * Secondary method to convert values to specific types. Only used if the conversion needs additional informations, e.g. lost generic types, date format,
	 * string encoding.
	 * 
	 * @param expectedType
	 *            Conversion target type.
	 * @param value
	 *            Value to be converted.
	 * @param additionalInformation
	 *            Additional information needed for this conversion.
	 * @return Representation of the given value as the target type.
	 */
	<T> T convertValueToType(Class<T> expectedType, Object value, Object additionalInformation);
}
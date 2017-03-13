package com.koch.ambeth.ioc.bytecode;

/**
 * Allows to enhance classes or interfaces at runtime. It specifically allows to implement
 * interfaces at runtime by bytecode enhancement. It also allows to sub-class existing classes and
 * implement additional interfaces, define fields, override or redefine methods according to the
 * intended enhancement process
 *
 * The whole enhancement process can be fully configured by linking instances of
 * <code>com.koch.ambeth.bytecode.behavior.IBytecodeBehavior</code> to
 * <code>com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorExtendable</code>.
 */
public interface IBytecodeEnhancer {
	/**
	 * Checks whether the given type is an enhanced type. Which means that this type is created at
	 * runtime by a previous call to getEnhancedType(...)
	 *
	 * @param potentiallyEnhancedType The type which is evaluated to be enhanced
	 * @return true if the given type is enhanced
	 */
	boolean isEnhancedType(Class<?> potentiallyEnhancedType);

	/**
	 * Evaluates the base type. A base type is the type which is the initial source of a potential
	 * enhancement hierarchy. It specifically means that the base type can be found in corresponding a
	 * compile-time class file
	 *
	 * @param potentiallyEnhancedType The type which is analyzed for its base type
	 * @return The base type of the given type. May be the same given instance if the type is not
	 *         enhanced
	 */
	Class<?> getBaseType(Class<?> potentiallyEnhancedType);

	/**
	 * Identical to a call to getEnhancedType(typeToEnhance,typeToEnhance.getName(),enhancementHint)
	 *
	 * Enhances the given type. The explicit enhancement algorithm executed at runtime can be
	 * configured by linking instances of
	 * <code>com.koch.ambeth.bytecode.behavior.IBytecodeBehavior</code> to
	 * <code>com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorExtendable</code>.
	 *
	 * @param typeToEnhance The given type which should be enhanced. The type may already be enhanced
	 *        by any enhancement algorithm. In this case the base type of the resulting enhanced type
	 *        is identical to the existing base type of the given enhanced type
	 * @param hint Allows to specify hints which allows registered IBytecodeBehaviors to decide
	 *        whether they "want" enhance the given type by their own business rule
	 * @return The enhanced type containing all content which has been produced by applied
	 *         IBytecodeBehaviors. If not a single IBytecodeBehavior has matched the returned type is
	 *         the same as the given type
	 */
	Class<?> getEnhancedType(Class<?> typeToEnhance, IEnhancementHint hint);

	/**
	 * Enhances the given type. The explicit enhancement algorithm executed at runtime can be
	 * configured by registering instances of
	 * <code>com.koch.ambeth.bytecode.behavior.IBytecodeBehavior</code> to
	 * <code>com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorExtendable</code>.
	 *
	 * @param typeToEnhance The given type which should be enhanced. The type may already be enhanced
	 *        by any enhancement algorithm. In this case the base type of the resulting enhanced type
	 *        is identical to the existing base type of the given enhanced type
	 * @param newTypeNamePrefix Specifies how the name of the enhanced type has to be prefixed. The
	 *        enhanced type name is produced by the following pattern:
	 *        newTypeNamePrefix$A&amp;number&amp; where 'number' may be any positive integer based on
	 *        the complexity of the applied IBytecodeBehaviors.
	 * @param hint Allows to specify hints which allows registered IBytecodeBehaviors to decide
	 *        whether they "want" enhance the given type by their own business rule
	 * @return The enhanced type containing all content which has been produced by applied
	 *         IBytecodeBehaviors. If not a single IBytecodeBehavior has matched the returned type is
	 *         the same as the given type
	 */
	Class<?> getEnhancedType(Class<?> typeToEnhance, String newTypeNamePrefix, IEnhancementHint hint);

	/**
	 * Checks whether a given interface will be available by enhancement by at least one instance of
	 * the registered <code>com.koch.ambeth.bytecode.behavior.IBytecodeBehavior</code>.
	 *
	 * @param enhancementType The enhancement interface to check for support
	 * @return true if the given interface is supported by an available IBytecodeBehavior. This does
	 *         not necessarily mean that all calls to getEnhancedType(...) will return an enhanced
	 *         type which really implements the given interface: This is dependent on the given
	 *         IEnhancementHint and the corresponding business rule of the IBytecodeBehavior
	 */
	boolean supportsEnhancement(Class<?> enhancementType);
}

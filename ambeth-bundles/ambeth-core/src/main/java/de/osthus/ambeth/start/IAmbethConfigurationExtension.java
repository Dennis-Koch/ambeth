package de.osthus.ambeth.start;

import de.osthus.ambeth.Ambeth;

/**
 * <p>
 * <b>If you implement this interface really read this text!</b>
 * </p>
 * <p>
 * An implementing class is hooked directly in the fluent API of the Ambeth startup configuration process. So it has to conform to the design of this API and
 * the following points.
 * </p>
 * <ul>
 * <li>Store the set {@link Ambeth} instance in a field.</li>
 * <li>Every method has to have {@link IAmbethConfiguration} or the extension type as the return type.</li>
 * <li>If it returns {@link IAmbethConfiguration} the Ambeth instance has to be returned to continue the normal API.</li>
 * <li>If it returns the extension type the extension instance (this) has to be returned to continue the extended API.</li>
 * <li>The extension API has to end returning the Ambeth instance.</li>
 * <li>The instance is hooked deeply into the startup process - be aware of what you do!</li>
 * </ul>
 */
public interface IAmbethConfigurationExtension
{
	void setAmbethConfiguration(Ambeth ambethConfiguration);
}

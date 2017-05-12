using System;

namespace De.Osthus.Ambeth.Util
{
    /// <summary>
    /// Provides an extension point for instances of {@link IDedicatedConverter} being used by the implementation of {@link IConversionHelper}
    /// 
    /// The implementation of {@link IConversionHelper} works with bi-directional polymorphic evaluation, example:
    ///
    /// Registering a {@link IDedicatedConverter} with sourceType=java.lang.Number and targetType=java.sql.Date will match if the {@link IConversionHelper} gets
    /// called to convert an instance of java.lang.Double to java.util.Date.
    /// 
    /// This is due to the fact that 1) the registered sourceType (Number) is inherited by the given value to convert (Double) 2) and the requested targetType
    /// (java.util.Date) is extended by the registered targetType (java.sql.Date) and therefore satisfies the conversion request.
    /// 
    /// <code>
    /// de.osthus.ambeth.util.IDedicatedConverter myConverter = ...<br>
    /// de.osthus.ambeth.ioc.IServiceContext serviceContext = ...<br>
    /// serviceContext.link(myConverter).to(de.osthus.ambeth.util.IDedicatedConverterExtendable.class).with(java.lang.Number.class, java.sql.Date.class);<br>
    /// <br>
    /// ...<br>
    /// <br>
    /// de.osthus.ambeth.util.IConversionHelper conversionHelper = serviceContext.getService(de.osthus.ambeth.util.IConversionHelper);<br>
    /// Object myValue = new java.lang.Double(1234567890);
    /// 
    /// // the following call to 'conversionHelper' from anywhere in the application calls the given 'myConverter' internally to convert java.lang.Double to java.util.Date
    /// java.util.Date myConvertedValue = conversionHelper.convertValueToType(java.util.Date.class, myValue);
    /// </code>
    /// </summary>
    public interface IDedicatedConverterExtendable
    {
        /// <summary>
        /// Registers a given {@link IDedicatedConverter} at this extension point.
	    /// 
	    /// If used during IoC container startup:
	    /// 
	    /// <code>
	    /// IBeanContextFactory beanContextFactory = ...;<br>
	    /// IBeanConfiguration myDedicatedConverter = ...;<br>
	    /// beanContextFactory.link(myDedicatedConverter).to(IDedicatedConverterExtendable.class).with(S.class, T.class);<br>
	    /// </code>
	    /// 
	    /// If used at runtime (after IoC container startup):
	    /// 
	    /// <code>
	    /// IServiceContext serviceContext = ...;<br>
	    /// IDedicatedConverter myDedicatedConverter = ...;<br>
	    /// serviceContext.link(myDedicatedConverter).to(IDedicatedConverterExtendable.class).with(S.class, T.class);<br>
	    /// </code>
        /// </summary>
        /// <param name="dedicatedConverter">given {@link IDedicatedConverter} to register</param>
        /// <param name="sourceType">Most abstract sourceType the given {@link IDedicatedConverter} claims to be able to convert from</param>
        /// <param name="targetType">Most specific targetType the given {@link IDedicatedConverter} claims to be able to convert to</param>
        void RegisterDedicatedConverter(IDedicatedConverter dedicatedConverter, Type sourceType, Type targetType);

        /// <summary>
        /// Unregisters a given {@link IDedicatedConverter} from this extension point. Note that the arguments (sourceType / targetType) have to match exactly to a
	    /// previous call to register.
	    /// 
	    /// The following applies for all extension points being accessed via the Link-API:<br>
	    /// If a registration has been done via the Link-API of the IoC container either at startup or runtime and if the extension should extend for the lifetime of
	    /// the container there is no need to manually unregister the extension. The IoC container will take care of correct unregistering at disposing-time of the
	    /// whole container and therefore addressing potential memory leaks or inconsistent component relationships automatically.
        /// </summary>
        /// <param name="dedicatedConverter">given {@link IDedicatedConverter} to register</param>
        /// <param name="sourceType">Most abstract sourceType the given {@link IDedicatedConverter} claims to be able to convert from</param>
        /// <param name="targetType">Most specific targetType the given {@link IDedicatedConverter} claims to be able to convert to</param>
        void UnregisterDedicatedConverter(IDedicatedConverter dedicatedConverter, Type sourceType, Type targetType);
    }
}
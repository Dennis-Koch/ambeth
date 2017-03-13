package com.koch.ambeth.xml.converter;

// package com.koch.ambeth.xml.converter;
//
// import javax.xml.datatype.DatatypeFactory;
// import javax.xml.datatype.XMLGregorianCalendar;
// import com.koch.ambeth.ioc.IInitializingBean;
// import com.koch.ambeth.log.ILogger;
// import com.koch.ambeth.log.LoggerFactory;
// import com.koch.ambeth.util.IDedicatedConverter;
//
// public class XmlGregorianCalendarConverter implements IInitializingBean, IDedicatedConverter
// {
// @SuppressWarnings("unused")
// @LogInstance(XmlGregorianCalendarConverter.class) private ILogger log;
//
// protected DatatypeFactory datatypeFactory;
//
// @Override
// public void afterPropertiesSet() throws Throwable
// {
// datatypeFactory = DatatypeFactory.newInstance();
// }
//
// @Override
// public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value)
// {
// if (XMLGregorianCalendar.class.equals(sourceType) && String.class.equals(expectedType))
// {
// XMLGregorianCalendar source = (XMLGregorianCalendar) value;
// return source.toXMLFormat();
// }
// else if (String.class.equals(sourceType) && XMLGregorianCalendar.class.equals(expectedType))
// {
// String source = (String) value;
//
// return datatypeFactory.newXMLGregorianCalendar(source);
// }
// return value;
// }
// }

package com.koch.ambeth.xml.converter;

/*-
 * #%L
 * jambeth-xml
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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

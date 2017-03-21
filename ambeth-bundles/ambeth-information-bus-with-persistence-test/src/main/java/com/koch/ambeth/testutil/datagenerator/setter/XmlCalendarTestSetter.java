package com.koch.ambeth.testutil.datagenerator.setter;

/*-
 * #%L
 * jambeth-information-bus-with-persistence-test
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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;


/**
 * Sets or tests a Date property. If a <code>String</code> argument with key
 * <code>StringTestSetter.class</code> is given, the String is added to the propertyName.
 *
 * @author stefan.may
 *
 */
public class XmlCalendarTestSetter extends AbstractTestSetter implements IInitializingBean {

	private DateTestSetter dateTestSetter;

	public XmlCalendarTestSetter() {
		super(XMLGregorianCalendar.class);
	}

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(dateTestSetter, "dateTestSetter");
	}

	public void setDateTestSetter(DateTestSetter dateTestSetter) {
		this.dateTestSetter = dateTestSetter;
	}

	@Override
	public Object createParameter(String propertyName, Map<Object, Object> arguments) {
		Date dateGenerated = (Date) dateTestSetter.createParameter(propertyName, arguments);

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(dateGenerated);
		try {
			XMLGregorianCalendar xmlcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			return xmlcal;
		}
		catch (DatatypeConfigurationException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}

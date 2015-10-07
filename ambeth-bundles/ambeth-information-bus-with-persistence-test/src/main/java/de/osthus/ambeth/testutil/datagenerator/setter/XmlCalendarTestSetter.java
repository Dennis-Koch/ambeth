package de.osthus.ambeth.testutil.datagenerator.setter;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.util.ParamChecker;


/**
 * Sets or tests a Date property. If a <code>String</code> argument with key <code>StringTestSetter.class</code> is
 * given, the String is added to the propertyName.
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
		c.setTime((Date) dateGenerated);
		try
		{
			XMLGregorianCalendar xmlcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			return xmlcal;
		}
		catch (DatatypeConfigurationException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}

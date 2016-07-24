package de.osthus.ambeth.query.squery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.ISortDescriptor;
import de.osthus.ambeth.filter.model.PagingRequest;
import de.osthus.ambeth.filter.model.SortDescriptor;
import de.osthus.ambeth.filter.model.SortDirection;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.query.squery.ioc.SqueryIocModule;
import de.osthus.ambeth.query.squery.model.Person;
import de.osthus.ambeth.query.squery.service.IPersonService;
import de.osthus.ambeth.query.squery.service.PersonService;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@SQLStructure("PersonQuery_structure.sql")
@SQLData("PersonQuery_data.sql")
@TestModule(SqueryIocModule.class)
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/query/squery/PersonQuery_orm.xml"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.persistence.jdbc.connection.LogPreparedStatementInterceptor", value = "DEBUG") })
public class PersonQueryTest extends AbstractInformationBusWithPersistenceTest
{
	@Autowired
	protected IPersonService personService;

	private static final int COUNT_OF_PERSON = 10;

	private static final String FIRST_PERSION_NAME = "person_name_1";

	private static final String PERSION_NAME_START = "person_name_";

	@Test
	public void testFindAll() throws Exception
	{
		List<Person> all = personService.findAll();

		assertFalse(all.isEmpty());
		assertEquals(COUNT_OF_PERSON, all.size());
	}

	@Test
	public void testFindByName() throws Exception
	{
		Integer firstId = 1;
		Person findById = personService.findById(firstId);
		assertEquals(firstId, findById.getId());

		List<Person> findByName = personService.findByName(FIRST_PERSION_NAME);
		assertEquals(1, findByName.size());
		assertEquals(FIRST_PERSION_NAME, findByName.get(0).getName());

		List<Person> findByNameIsNull = personService.findByNameIsNull();
		assertEquals(1, findByNameIsNull.size());
		assertEquals(Integer.valueOf(10), findByNameIsNull.get(0).getId());

		List<Person> findByNameStartWith = personService.findByNameStartWith(PERSION_NAME_START);
		assertEquals(9, findByNameStartWith.size());

		String nameContains = "person_name_1";
		List<Person> findByNameContainedIn = personService.findByNameContains(nameContains);
		assertEquals(2, findByNameContainedIn.size());
		for (Person person : findByNameContainedIn)
		{
			assertTrue(person.getName().contains(nameContains));
		}
	}

	@Test
	public void testMultiConditionQuery() throws Exception
	{
		Integer maxVersion = 5;
		String[] nameIn = { "person_name_8", "person_name_11", "not exists name" };
		int minAge = 22;
		List<Person> findByVersionLtOrAgeGeAndNameIn = personService.findByVersionLtOrAgeGeAndNameIn(maxVersion, minAge, nameIn);
		Set<String> nameInSet = new HashSet<String>(nameIn);
		for (Person person : findByVersionLtOrAgeGeAndNameIn)
		{
			assertTrue(person.getVersion() < maxVersion || (person.getAge() >= minAge && nameInSet.contains(person.getName())));
		}
	}

	@Test
	public void testSpecialNameContainsAndOr() throws Exception
	{
		List<Person> findByHaveAndriod = personService.findByHaveAndriod(true);
		assertFalse(findByHaveAndriod.isEmpty());
		for (Person person : findByHaveAndriod)
		{
			assertTrue(person.getHaveAndriod());
		}

		List<Person> findByHaveOrange = personService.findByHaveOrange(false);
		assertFalse(findByHaveOrange.isEmpty());
		for (Person person : findByHaveOrange)
		{
			assertFalse(person.getHaveOrange());
		}
		Integer minAge = 16;
		List<Person> findByHaveOrangeOrAgeGe = personService.findByHaveOrangeOrAgeGe(true, minAge);
		assertFalse(findByHaveOrangeOrAgeGe.isEmpty());
		for (Person person : findByHaveOrangeOrAgeGe)
		{
			assertTrue(person.getHaveOrange() || person.getAge() >= minAge);
		}
	}

	@Test
	public void testNestFieldQuery() throws Exception
	{
		String addreasName = "addreas_2";
		List<Person> findByHomeAddressName = personService.findByHomeAddressName(addreasName);
		assertFalse(findByHomeAddressName.isEmpty());
		for (Person person : findByHomeAddressName)
		{
			assertEquals(addreasName, person.getHome().getAddress().getName());
		}

		String streatName = "streat_4";
		List<Person> findByHomeAddressStreat = personService.findByHomeAddressStreat(streatName);
		assertFalse(findByHomeAddressStreat.isEmpty());
		for (Person person : findByHomeAddressStreat)
		{
			assertEquals(streatName, person.getHomeAddress().getStreat());
		}
	}

	@Test
	public void testCountBy() throws Exception
	{
		long countByNameContains = personService.countByNameContains(PERSION_NAME_START);
		List<Person> findByNameContains = personService.findByNameContains(PERSION_NAME_START);
		assertEquals(findByNameContains.size(), countByNameContains);

		int countByNameStartWith = personService.countByNameStartWith(FIRST_PERSION_NAME);
		List<Person> findByNameStartWith = personService.findByNameStartWith(FIRST_PERSION_NAME);
		assertEquals(findByNameStartWith.size(), countByNameStartWith);
	}

	@Test
	public void testOrderBy() throws Exception
	{
		List<Person> singleOrderlist = personService.findAllSortByAgeDesc();
		assertTrue(singleOrderlist.size() > 1);
		for (int i = 1; i < singleOrderlist.size(); i++)
		{
			int agePre = singleOrderlist.get(i - 1).getAge();
			int ageCurrent = singleOrderlist.get(i).getAge();
			assertTrue(agePre >= ageCurrent);
		}
		List<Person> doubleSortList = personService.findByNameStartWithOrderByAgeAscNameDesc(PERSION_NAME_START);
		assertTrue(doubleSortList.size() > 1);
		for (int i = 1; i < doubleSortList.size(); i++)
		{
			int agePre = doubleSortList.get(i - 1).getAge();
			int ageCurrent = doubleSortList.get(i).getAge();
			String namePre = doubleSortList.get(i - 1).getName();
			String nameCurrent = doubleSortList.get(i).getName();
			assertTrue(agePre <= ageCurrent);
			if (agePre == ageCurrent)
			{
				assertTrue(namePre.compareTo(nameCurrent) >= 0);
			}
		}

		ISortDescriptor sort = new SortDescriptor().withMember("Age").withSortDirection(SortDirection.DESCENDING);
		List<Person> mixSortList = personService.findAllSortByVersion(sort);
		assertTrue(mixSortList.size() > 1);
		for (int i = 1; i < mixSortList.size(); i++)
		{
			int versionPre = mixSortList.get(i - 1).getVersion();
			int versionCurrent = mixSortList.get(i).getVersion();
			int agePre = mixSortList.get(i - 1).getAge();
			int ageCurrent = mixSortList.get(i).getAge();
			assertTrue(versionPre <= versionCurrent);
			if (versionPre == versionCurrent)
			{
				assertTrue(agePre >= ageCurrent);
			}
		}
	}

	@Test
	public void testPageQuery() throws Exception
	{
		int pageSize = 4;
		int pageNumber = 1;
		IPagingRequest request = new PagingRequest().withSize(pageSize).withNumber(pageNumber);
		ISortDescriptor sort = new SortDescriptor().withMember("Name").withSortDirection(SortDirection.DESCENDING);

		IPagingResponse<Person> page = personService.findByNameStartWith(PERSION_NAME_START, request, sort);
		List<Person> allStartList = personService.findByNameStartWithSortByNameDesc(PERSION_NAME_START);
		assertEquals(page.getNumber(), pageNumber);
		List<IObjRef> result = page.getRefResult();
		for (int i = 0; i < result.size(); i++)
		{
			assertEquals(result.get(i).getId(), allStartList.get((pageSize * pageNumber) + i).getId());
		}

		int minAge = 13;
		List<Person> pageList = personService.findByAgeGe(minAge, request, sort);
		List<Person> ageGeList = personService.findByAgeGeSortByNameDesc(minAge);
		for (int i = 0; i < pageList.size(); i++)
		{
			assertEquals(pageList.get(i).getId(), ageGeList.get((pageSize * pageNumber) + i).getId());
			assertTrue(pageList.get(i).getAge() >= minAge);
		}
	}

	@Test
	public void testNullValue()
	{
		Integer maxVersion = 7;
		String[] nameIn = null;
		List<Person> list = personService.findByVersionLtOrAgeGeAndNameIn(maxVersion, null, nameIn); // only age take part in query
		for (Person person : list)
		{
			assertTrue(person.getVersion() < maxVersion);
			assertNotNull(person.getAge());
			assertNotNull(person.getName());
		}
	}

	@Test
	public void testDateQuery() throws Exception
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, 9, 3, 13, 12, 1);
		Date date = calendar.getTime();
		List<Person> dateAt = personService.findByModifyTimeDateAt(date);
		for (Person person : dateAt)
		{
			assertEquals(toMinTime(person.getModifyTime()).compareTo(date), 0);
		}

		List<Person> dateLt = personService.findByModifyTimeDateLt(date);
		for (Person person : dateLt)
		{
			assertTrue(person.getModifyTime().compareTo(date) < 0);
		}

		List<Person> dateLe = personService.findByModifyTimeDateLe(date);
		for (Person person : dateLe)
		{
			assertTrue(person.getModifyTime().compareTo(toMaxTime(date)) < 0);
		}

		List<Person> dateGt = personService.findByModifyTimeDateGt(date);
		for (Person person : dateGt)
		{
			assertTrue(person.getModifyTime().compareTo(toMaxTime(date)) > 0);
		}

		List<Person> dateGe = personService.findByModifyTimeDateGe(date);
		for (Person person : dateGe)
		{
			assertTrue(person.getModifyTime().compareTo(date) >= 0);
		}
	}

	@Test
	public void testNotSqueryMethod() throws Exception
	{
		String value = personService.someConcreteMethod("any value");
		assertEquals(PersonService.CONCRETE_METHOD_ERROR, value);
	}

	private static Date toMinTime(Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	private static Date toMaxTime(Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}
}

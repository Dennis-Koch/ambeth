package de.osthus.ambeth.chemspider;

import org.junit.Test;

import com.chemspider.www.MassSpecAPIStub;
import com.chemspider.www.MassSpecAPIStub.GetExtendedCompoundInfo;
import com.chemspider.www.MassSpecAPIStub.GetExtendedCompoundInfoResponse;
import com.chemspider.www.MassSpecAPIStub.GetRecordMol;
import com.chemspider.www.MassSpecAPIStub.GetRecordMolResponse;
import com.chemspider.www.MassSpecAPIStub.SearchByFormula;
import com.chemspider.www.MassSpecAPIStub.SearchByFormulaResponse;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractIocTest;

public class ChemSpiderTest extends AbstractIocTest
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Test
	public void test() throws Throwable
	{
		String token = "ab980b4a-bc7b-464e-97d0-48627bcee3bb";
		MassSpecAPIStub ab = new MassSpecAPIStub();

		SearchByFormula searchByFormula = new SearchByFormula();
		searchByFormula.setFormula("H2O");
		SearchByFormulaResponse searchByFormula2 = ab.searchByFormula(searchByFormula);
		String[] response = searchByFormula2.getSearchByFormulaResult().getString();

		GetRecordMol getRecordMol = new GetRecordMol();
		getRecordMol.setCalc3D(false);
		getRecordMol.setCsid(response[0]);
		getRecordMol.setToken(token);
		GetRecordMolResponse recordMol = ab.getRecordMol(getRecordMol);

		GetExtendedCompoundInfo getExtendedCompoundInfo = new GetExtendedCompoundInfo();
		getExtendedCompoundInfo.setCSID(Integer.parseInt(response[0]));
		getExtendedCompoundInfo.setToken(token);
		GetExtendedCompoundInfoResponse extendedCompoundInfo = ab.getExtendedCompoundInfo(getExtendedCompoundInfo);
		System.out.println();
	}
}

package com.koch.ambeth.chemspider;

/*-
 * #%L
 * jambeth-chemspider-test
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

import org.junit.Test;

import com.chemspider.www.MassSpecAPIStub;
import com.chemspider.www.MassSpecAPIStub.GetExtendedCompoundInfo;
import com.chemspider.www.MassSpecAPIStub.GetExtendedCompoundInfoResponse;
import com.chemspider.www.MassSpecAPIStub.GetRecordMol;
import com.chemspider.www.MassSpecAPIStub.GetRecordMolResponse;
import com.chemspider.www.MassSpecAPIStub.SearchByFormula;
import com.chemspider.www.MassSpecAPIStub.SearchByFormulaResponse;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.testutil.AbstractIocTest;

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

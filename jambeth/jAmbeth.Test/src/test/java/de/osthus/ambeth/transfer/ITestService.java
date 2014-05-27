package de.osthus.ambeth.transfer;

import java.util.Date;
import java.util.List;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.model.MaterialGroup;

@XmlType
public interface ITestService
{
	void noParamNoReturn();

	void primitiveParamNoReturn(int param);

	void dateParamNoReturn(Date param);

	void primitiveArrayParamNoReturn(int[] param);

	void primitiveListParamNoReturn(List<Integer> param);

	void entityParamNoReturn(MaterialGroup param);

	void entityWithRelationParamNoReturn(Material param);

	void mixedParamsNoReturn(int number, Material material1, String text, MaterialGroup materialGroup, Material material2, Date date);

	int noParamPrimitiveReturn();

	Date noParamDateReturn();

	int[] noParamPrimitiveArrayReturn();

	List<Integer> noParamPrimitiveListReturn();

	MaterialGroup noParamEntityReturn();

	Material noParamEntityWithRelationReturn();
}

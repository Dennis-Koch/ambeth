package com.koch.ambeth.datachange;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.transfer.DataChangeEntry;
import com.koch.ambeth.datachange.transfer.DataChangeEvent;
import com.koch.ambeth.service.merge.model.IObjRef;

public class DataChangeDeriveTest {
	@Test
	public void deriveByType() {
		DataChangeEvent dce = DataChangeEvent.create(-1, -1, -1);
		dce.getUpdates().add(new DataChangeEntry(Number.class, 0, 1, 1));
		dce.getUpdates().add(new DataChangeEntry(Object.class, 0, 2, 1));
		dce.getUpdates().add(new DataChangeEntry(Long.class, 0, 3, 1));
		dce.getUpdates().add(new DataChangeEntry(Number.class, 0, 4, 1));
		dce.getUpdates().add(new DataChangeEntry(Long.class, 0, 5, 1));

		IDataChange floatDce = dce.derive(Float.class);
		Assert.assertTrue(floatDce.isEmpty());

		IDataChange numberDce = dce.derive(Number.class);
		Assert.assertEquals(4, numberDce.getAll().size());

		IDataChange objectDce = dce.derive(Object.class);
		Assert.assertEquals(dce.getAll().size(), objectDce.getAll().size());

		IDataChange longDce = dce.derive(Long.class);
		Assert.assertEquals(2, longDce.getAll().size());
	}

	@Test
	public void deriveById() {
		DataChangeEvent dce = DataChangeEvent.create(-1, -1, -1);
		dce.getUpdates().add(new DataChangeEntry(Number.class, 0, 1, 1));
		dce.getUpdates().add(new DataChangeEntry(Number.class, IObjRef.PRIMARY_KEY_INDEX, 1, 1));
		dce.getUpdates().add(new DataChangeEntry(Long.class, 0, 1, 1));
		dce.getUpdates().add(new DataChangeEntry(Long.class, IObjRef.PRIMARY_KEY_INDEX, 1, 1));

		IDataChange derive = dce.derive(IObjRef.PRIMARY_KEY_INDEX, 1);
		Assert.assertEquals(2, derive.getAll().size());

		Assert.assertEquals(1, derive.getUpdates().get(0).getId());
		Assert.assertEquals(1, derive.getUpdates().get(1).getId());
	}
}

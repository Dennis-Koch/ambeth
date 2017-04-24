package com.koch.ambeth.merge.config;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.orm.IOrmConfigGroup;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;

public class IndependentEntityMetaDataReader extends AbstractEntityMetaDataReader
		implements IStartingBean {
	public static final String P_FILE_NAME = "FileName";

	@LogInstance
	private ILogger log;

	protected String fileName = null;

	@Override
	public void afterStarted() throws Throwable {
		if (fileName != null) {
			IOrmConfigGroup ormConfigGroup = ormConfigGroupProvider.getOrmConfigGroup(fileName);
			readConfig(ormConfigGroup);
		}
	}

	@Property(name = ServiceConfigurationConstants.mappingFile, mandatory = false)
	public void setFileName(String fileName) {
		if (this.fileName != null) {
			throw new IllegalArgumentException(IndependentEntityMetaDataReader.class.getSimpleName()
					+ " already configured! Tried to set the config file '" + fileName
					+ "'. File name is already set to '" + this.fileName + "'");
		}
		this.fileName = fileName;
	}
}

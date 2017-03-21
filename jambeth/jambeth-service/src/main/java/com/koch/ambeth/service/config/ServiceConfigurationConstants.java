package com.koch.ambeth.service.config;

/*-
 * #%L
 * jambeth-service
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

import com.koch.ambeth.util.annotation.ConfigurationConstantDescription;
import com.koch.ambeth.util.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class ServiceConfigurationConstants {
	@ConfigurationConstantDescription("TODO")
	public static final String ServiceBaseUrl = "service.baseurl";

	@ConfigurationConstantDescription("TODO")
	public static final String ServiceProtocol = "service.protocol";

	@ConfigurationConstantDescription("TODO")
	public static final String ServiceHostName = "service.host";

	@ConfigurationConstantDescription("TODO")
	public static final String ServiceHostPort = "service.port";

	@ConfigurationConstantDescription("TODO")
	public static final String ProcessServiceBeanActive = "process.service.active";

	@ConfigurationConstantDescription("TODO")
	public static final String TypeInfoProviderType = "service.typeinfoprovider.type";

	@ConfigurationConstantDescription("TODO")
	public static final String ServiceRemoteInterceptorType = "service.remoteinterceptor.type";

	@ConfigurationConstantDescription("TODO")
	public static final String ToOneDefaultCascadeLoadMode = "cache.cascadeload.toone";

	@ConfigurationConstantDescription("TODO")
	public static final String ToManyDefaultCascadeLoadMode = "cache.cascadeload.tomany";

	@ConfigurationConstantDescription("TODO")
	public static final String WrapAllInteractions = "ioc.logged";

	@ConfigurationConstantDescription("TODO")
	public static final String LogShortNames = "log.shortnames";

	@ConfigurationConstantDescription("TODO")
	public static final String NetworkClientMode = "network.clientmode";

	@ConfigurationConstantDescription("TODO")
	public static final String SlaveMode = "service.slavemode";

	@ConfigurationConstantDescription("TODO")
	public static final String OfflineMode = "network.offlinemode";

	@ConfigurationConstantDescription("TODO")
	public static final String OfflineModeSupported = "network.offlinemode.supported";

	@ConfigurationConstantDescription("TODO")
	public static final String GenericTransferMapping = "transfermapping.generic";

	@ConfigurationConstantDescription("TODO")
	public static final String IndependentMetaData = "metadata.independent";

	@ConfigurationConstantDescription("TODO")
	public static final String mappingFile = "mapping.file";

	@Deprecated
	@ConfigurationConstantDescription("TODO")
	public static final String mappingResource = "mapping.resource";

	@ConfigurationConstantDescription("TODO")
	public static final String valueObjectFile = "valueobject.file";

	@Deprecated
	@ConfigurationConstantDescription("TODO")
	public static final String valueObjectResource = "valueobject.resource";

	public static final String UserName = "ambeth.remoting.username";

	public static final String Password = "ambeth.remoting.password";

	private ServiceConfigurationConstants() {
		// Intended blank
	}
}

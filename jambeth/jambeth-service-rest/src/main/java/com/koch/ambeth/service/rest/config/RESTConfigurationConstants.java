package com.koch.ambeth.service.rest.config;

/*-
 * #%L
 * jambeth-service-rest
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

import com.koch.ambeth.util.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class RESTConfigurationConstants {
    public static final String HttpUseClient = "rest.http.client";

    public static final String HttpAcceptEncodingZipped = "rest.http.accept-encoding.zipped";

    public static final String HttpContentEncodingZipped = "rest.http.content-encoding.zipped";

    public static final String SslKeyStoreFile = "rest.https.keystore.file";

    public static final String SslKeyStorePassword = "rest.https.keystore.password";

    public static final String SslContextFactoryType = "rest.https.sslcontextfactorytype";

    private RESTConfigurationConstants() {
        // Intended blank
    }
}

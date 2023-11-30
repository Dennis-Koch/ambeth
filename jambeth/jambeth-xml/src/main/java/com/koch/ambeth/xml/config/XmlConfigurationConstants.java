package com.koch.ambeth.xml.config;

/*-
 * #%L
 * jambeth-xml
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

import com.koch.ambeth.ioc.annotation.ApplicationModule;
import com.koch.ambeth.util.annotation.ConfigurationConstants;
import com.koch.ambeth.xml.util.ClasspathScanner;
import jakarta.xml.bind.annotation.XmlRootElement;

@ConfigurationConstants
public final class XmlConfigurationConstants {
    /**
     * Defines in which packages the Ambeth {@link ClasspathScanner} scans for classes which implement
     * or are annotated with a given class (e.g {@link XmlRootElement}, {@link ApplicationModule}).
     * Multiple patterns can be provided by separating them with a semicolon ';', each pattern has to
     * be a regular expression. The default pattern is <code>"com/koch/.*"</code>
     */
    public static final String PackageScanPatterns = "ambeth.xml.transfer.pattern";

    private XmlConfigurationConstants() {
        // Intended blank
    }
}

---
-- #%L
-- jambeth-test
-- %%
-- Copyright (C) 2017 Koch Softwaredevelopment
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-- #L%
---
-- Business object
INSERT INTO "BL_ENTITY_TYPE" (ID, VERSION, NAME, INTERFACES, IS_CLASS) VALUES (1, 1, 'com.koch.ambeth.persistence.blueprint.TestClass',  STRING_ARRAY('com.koch.ambeth.model.IAbstractEntity'), 0);

INSERT INTO "BL_ENTITY_PROPERTY" (ID, VERSION, NAME, TYPE, "ORDER", READONLY)  VALUES (1, 1, 'Something', 'java.lang.String', 0, 0);
INSERT INTO "BL_LINK_ENTITY_TO_ENTITY_PROP" (ENTITY_ID, ENTITY_PROP_ID) VALUES ('1', '1');

INSERT INTO "BL_ENTITY_ANNO" (ID, VERSION, TYPE)  VALUES (1, 1, 'com.koch.ambeth.audit.model.Audited');
INSERT INTO "BL_ENTITY_ANNO_PROPERTY" (ID, VERSION, NAME, VALUE)  VALUES (1, 1, 'value','true');

INSERT INTO "BL_LINK_ENTITY_TO_ANNO" (ENTITY_ID, ANNO_ID) VALUES ('1', '1');
INSERT INTO "BL_LINK_ANNO_TO_ANNO_PROP" (ANNO_ID, ANNO_PROP_ID) VALUES ('1', '1');

-- Value object
INSERT INTO "BL_ENTITY_TYPE" (ID, VERSION, NAME, SUPERCLASS, IS_CLASS) VALUES (2, 2, 'com.koch.ambeth.persistence.blueprint.TestClassV', 'com.koch.ambeth.persistence.blueprint.TransferEntity', 1);

INSERT INTO "BL_ENTITY_PROPERTY" (ID, VERSION, NAME, TYPE, "ORDER", READONLY)  VALUES (2, 2, 'Something', 'java.lang.String', 0, 0);
INSERT INTO "BL_LINK_ENTITY_TO_ENTITY_PROP" (ENTITY_ID, ENTITY_PROP_ID) VALUES ('2', '2');

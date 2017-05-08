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
INSERT INTO "DATA_CHANGE_EVENT" (ID, CHANGE_TIME, VERSION) VALUES ('1', '123456789', '1');

INSERT INTO "DATA_CHANGE_ENTRY" (ID, ENTITY_TYPE, ID_INDEX, OBJECT_ID, OBJECT_VERSION, UPDATE_PARENT, VERSION) VALUES ('11', '21', '-1', '2', '21', '1', '1');

INSERT INTO "ENTITY_TYPE" (ID, TYPE, VERSION) VALUES ('21', 'com.koch.ambeth.model.DataChangeEntryBO', '1');

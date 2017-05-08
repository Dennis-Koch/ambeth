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
INSERT INTO "MATERIAL_GROUP" (ID, BUID, NAME, VERSION) VALUES ('1', 'MG 1', 'MG 1', '1');

INSERT INTO "MATERIAL" (ID, BUID, NAME, VERSION, MATERIAL_GROUP, CREATED_BY, CREATED_ON) VALUES ('1', 'Material 1', 'Material 1', '1', '1', 'anonymous', to_timestamp('22.02.12 00:00:00,000000000','DD.MM.RR HH24:MI:SS,FF'));

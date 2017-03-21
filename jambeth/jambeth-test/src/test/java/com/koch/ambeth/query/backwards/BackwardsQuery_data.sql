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

INSERT INTO "JOIN_QUERY_ENTITY" (ID, VALUE_FIELD, VERSION) VALUES ('11', '55', '2');
INSERT INTO "JOIN_QUERY_ENTITY" (ID, VALUE_FIELD, PARENT, VERSION) VALUES ('12', '77', '11', '3');

INSERT INTO "QUERY_ENTITY" (ID, NAME, NEXT, JOIN_QUERY_ENTITY, LINK_TABLE_ENTITY, VERSION) VALUES ('1', 'name1', '2', '11', '21', '2');
INSERT INTO "QUERY_ENTITY" (ID, NAME, JOIN_QUERY_ENTITY, VERSION) VALUES ('2', 'name2', '12', '1');

INSERT INTO "LINK_TABLE_ENTITY" (ID, NAME, VERSION) VALUES ('21', 'name21', '1');

INSERT INTO "LINK_JQE_LTE" (LEFT_ID, RIGHT_ID) VALUES ('12', '21');

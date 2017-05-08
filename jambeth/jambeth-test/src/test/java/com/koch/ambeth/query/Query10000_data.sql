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
INSERT INTO "QUERY_ENTITY" (ID, NAME1, VERSION) VALUES ("QUERY_ENTITY_SEQ".NEXTVAL, 'A_10000_Name'||"QUERY_ENTITY_SEQ".NEXTVAL, '2');
INSERT INTO "QUERY_ENTITY" (ID, NAME1, VERSION) VALUES ("QUERY_ENTITY_SEQ".NEXTVAL, 'Q10000NameA'||"QUERY_ENTITY_SEQ".NEXTVAL, '2');
INSERT INTO "QUERY_ENTITY" (ID, NAME1, VERSION) VALUES ("QUERY_ENTITY_SEQ".NEXTVAL, 'Q_10000NameA'||"QUERY_ENTITY_SEQ".NEXTVAL, '2');
INSERT INTO "QUERY_ENTITY" (ID, NAME1, VERSION) VALUES ("QUERY_ENTITY_SEQ".NEXTVAL, 'Q_10000_Nam'||"QUERY_ENTITY_SEQ".NEXTVAL, '2');

--: loop = 5000
INSERT INTO "QUERY_ENTITY" (ID, NAME1, VERSION) VALUES ("QUERY_ENTITY_SEQ".NEXTVAL, 'Q_10000_Name'||"QUERY_ENTITY_SEQ".NEXTVAL, '2');

--: loop = 5000
INSERT INTO "QUERY_ENTITY" (ID, NAME1, VERSION) VALUES ("QUERY_ENTITY_SEQ".NEXTVAL, 'Q_10000_Name'||"QUERY_ENTITY_SEQ".NEXTVAL, '3');

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
INSERT INTO "QUERY_ENTITY" ("ID", "NAME1","NAME2", "FTNAME1", "CONTENT", "FK", "VERSION") VALUES ('1', 'name1_name2', 'name3', 'name1_name2', 1.0, '2', '2');
INSERT INTO "QUERY_ENTITY" ("ID", "NAME1","NAME2", "FTNAME1", "CONTENT", "VERSION") VALUES ('2', 'name1', 'name2', 'name1', 2.1, '1');
INSERT INTO "QUERY_ENTITY" ("ID", "NAME1", "NAME2", "FTNAME1", "CONTENT", "FK", "VERSION") VALUES ('3', 'name2', 'name3', 'name1', 3.2, '1', '2');
INSERT INTO "QUERY_ENTITY" ("ID", "NAME1", "NAME2", "FTNAME1", "CONTENT", "FK", "VERSION") VALUES ('4', 'name3', 'name1', 'name3', 4.3, '2', '1');
INSERT INTO "QUERY_ENTITY" ("ID", "NAME1", "NAME2", "FTNAME1", "CONTENT", "VERSION") VALUES ('5', 'name11', 'name1', 'name1', 5.4, '2');
INSERT INTO "QUERY_ENTITY" ("ID", "NAME1", "NAME2", "FTNAME1", "CONTENT", "FK", "VERSION") VALUES ('6', 'name1111', 'name1', 'name1', 5.4, '1', '2');

INSERT INTO "JOIN_QUERY_ENTITY" ("ID", "PARENT", "CONTENT", "JOIN_VALUE_1", "VERSION") VALUES ('1', '2', 3.14, 4, '2');
INSERT INTO "JOIN_QUERY_ENTITY" ("ID", "CONTENT", "JOIN_VALUE_1", "VERSION") VALUES ('2',		 2.7, 3, '3');
INSERT INTO "JOIN_QUERY_ENTITY" ("ID", "CONTENT", "JOIN_VALUE_1", "VERSION") VALUES ('3',		 0.125, 1, '1');

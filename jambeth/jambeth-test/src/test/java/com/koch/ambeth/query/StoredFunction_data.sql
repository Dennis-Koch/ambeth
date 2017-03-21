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
INSERT INTO "QUERY_ENTITY" (ID, FK, VERSION) VALUES ('1', '2', '2');
INSERT INTO "QUERY_ENTITY" (ID, VERSION) VALUES ('2', '1');
INSERT INTO "QUERY_ENTITY" (ID, FK, VERSION) VALUES ('3', '1', '2');
INSERT INTO "QUERY_ENTITY" (ID, FK, VERSION) VALUES ('4', '2', '1');
INSERT INTO "QUERY_ENTITY" (ID, VERSION) VALUES ('5', '2');

INSERT INTO "JOIN_QUERY_ENTITY" (ID, PARENT, VERSION) VALUES ('1', '2', '2');
INSERT INTO "JOIN_QUERY_ENTITY" (ID, VERSION) VALUES ('2', '3');
INSERT INTO "JOIN_QUERY_ENTITY" (ID, VERSION) VALUES ('3', '1');

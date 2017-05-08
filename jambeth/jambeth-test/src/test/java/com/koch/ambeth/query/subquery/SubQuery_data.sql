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
INSERT INTO "ENTITY_A" (ID, BUID, ENTITY_B, VERSION) VALUES ('1', 'BUID 1', '11', '1');
INSERT INTO "ENTITY_A" (ID, BUID, ENTITY_B, VERSION) VALUES ('2', 'BUID 2', '13', '1');
INSERT INTO "ENTITY_A" (ID, BUID, ENTITY_B, VERSION) VALUES ('3', 'BUID 3', '12', '5');
INSERT INTO "ENTITY_A" (ID, BUID, ENTITY_B, VERSION) VALUES ('4', 'BUID 4', '12', '1');

INSERT INTO "ENTITY_B" (ID, BUID, VERSION) VALUES ('11', 'BUID 11', '2');
INSERT INTO "ENTITY_B" (ID, BUID, VERSION) VALUES ('12', 'BUID 12', '5');
INSERT INTO "ENTITY_B" (ID, BUID, VERSION) VALUES ('13', 'BUID 13', '2');

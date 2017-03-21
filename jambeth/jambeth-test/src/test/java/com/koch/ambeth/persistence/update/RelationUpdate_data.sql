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
INSERT INTO "ENTITY_A" ("ID", "OTHER", "ENTITY_D", "VERSION") VALUES ('1', '12', '31', '1');
INSERT INTO "ENTITY_A" ("ID", "OTHER", "VERSION") VALUES ('2', '11', '1');

INSERT INTO "ENTITY_B" ("ID", "VERSION") VALUES ('11', '1');
INSERT INTO "ENTITY_B" ("ID", "VERSION") VALUES ('12', '1');
INSERT INTO "ENTITY_B" ("ID", "VERSION") VALUES ('13', '1');

INSERT INTO "ENTITY_C" ("ID", "VERSION") VALUES ('21', '1');
INSERT INTO "ENTITY_C" ("ID", "VERSION") VALUES ('22', '1');
INSERT INTO "ENTITY_C" ("ID", "VERSION") VALUES ('23', '1');
INSERT INTO "ENTITY_C" ("ID", "VERSION") VALUES ('24', '1');
INSERT INTO "ENTITY_C" ("ID", "VERSION") VALUES ('25', '1');
INSERT INTO "ENTITY_C" ("ID", "VERSION") VALUES ('26', '1');

INSERT INTO "ENTITY_D" ("ID", "VERSION") VALUES ('31', '1');

INSERT INTO "LINK_ENTITY_A_ENTITY_C" ("A_ID", "C_ID") VALUES ('1', '21');
INSERT INTO "LINK_ENTITY_A_ENTITY_C" ("A_ID", "C_ID") VALUES ('1', '22');
INSERT INTO "LINK_ENTITY_A_ENTITY_C" ("A_ID", "C_ID") VALUES ('1', '23');
INSERT INTO "LINK_ENTITY_A_ENTITY_C" ("A_ID", "C_ID") VALUES ('2', '24');
INSERT INTO "LINK_ENTITY_A_ENTITY_C" ("A_ID", "C_ID") VALUES ('2', '25');

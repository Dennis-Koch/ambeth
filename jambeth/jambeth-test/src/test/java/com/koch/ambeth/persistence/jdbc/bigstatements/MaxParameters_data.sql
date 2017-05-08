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
INSERT INTO "D_MATERIAL_GROUP" ("ID", "F_NAME", "VERSION") VALUES ('me', 'Metal', '1');
INSERT INTO "D_MATERIAL_GROUP" ("ID", "F_NAME", "VERSION") VALUES ('pl', 'Kunststof', '1');
UPDATE "D_MATERIAL_GROUP" SET "F_NAME" = 'Kunststoff', "VERSION" = '2' WHERE "ID" = 'pl' AND "VERSION" = '1';

INSERT INTO "D_MATERIAL" ("ID", "F_NAME", "VERSION", "F_MATERIAL_GROUP", "CREATED_BY", "CREATED_ON") VALUES ('1', 'test 1', '1', 'me', 'anonymous', to_timestamp('22.02.12 00:00:00,000000000','DD.MM.RR HH24:MI:SS,FF'));
INSERT INTO "D_MATERIAL" ("ID", "F_NAME", "VERSION", "F_MATERIAL_GROUP") VALUES ('2', 'test 2', '1', 'pl');
INSERT INTO "D_MATERIAL" ("ID", "F_NAME", "VERSION", "F_MATERIAL_GROUP") VALUES ('3', 'test 3', '2', 'pl');
INSERT INTO "D_MATERIAL" ("ID", "F_NAME", "VERSION", "F_MATERIAL_GROUP") VALUES ('4', 'test 4', '1', 'me');
INSERT INTO "D_MATERIAL" ("ID", "F_NAME", "VERSION", "F_MATERIAL_GROUP") VALUES ('5', 'test 5', '1', 'me');

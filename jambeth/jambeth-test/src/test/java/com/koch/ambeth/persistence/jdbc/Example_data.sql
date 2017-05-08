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

INSERT INTO "UNIT" ("ID", "NAME", "VERSION") VALUES ('1', 'mm', '1'); 
INSERT INTO "UNIT" ("ID", "NAME", "VERSION") VALUES ('2', 'cm', '1'); 
INSERT INTO "UNIT" ("ID", "NAME", "VERSION") VALUES ('3', 'm', '2'); 

INSERT INTO "LINK_MATERIAL_UNIT" ("LEFT_ID", "RIGHT_ID") VALUES ('1', '2');
INSERT INTO "LINK_MATERIAL_UNIT" ("LEFT_ID", "RIGHT_ID") VALUES ('2', '1');
INSERT INTO "LINK_MATERIAL_UNIT" ("LEFT_ID", "RIGHT_ID") VALUES ('3', '1');
INSERT INTO "LINK_MATERIAL_UNIT" ("LEFT_ID", "RIGHT_ID") VALUES ('4', '3');
INSERT INTO "LINK_MATERIAL_UNIT" ("LEFT_ID", "RIGHT_ID") VALUES ('5', '2');

-- Dynamic part of the setup process 

--: loop = 50
INSERT INTO "D_MATERIAL" ("ID", "F_NAME", "VERSION", "F_MATERIAL_GROUP") VALUES ("D_MATERIAL_SEQ".NEXTVAL, 'loop material', '1', 'me');

--: loop = 4
INSERT INTO "UNIT" ("ID", "NAME", "VERSION") VALUES ("UNIT_SEQ".NEXTVAL, 'loop unit', '1');

--: loop = 50
INSERT INTO "LINK_MATERIAL_UNIT" ("LEFT_ID", "RIGHT_ID") VALUES (
	(SELECT "ID" FROM "D_MATERIAL" WHERE NOT EXISTS 
		(SELECT "LEFT_ID" FROM "LINK_MATERIAL_UNIT" 
 		WHERE "LINK_MATERIAL_UNIT"."LEFT_ID" = "D_MATERIAL"."ID") AND ROWNUM = 1),
	(SELECT "ID" FROM (SELECT "ID" FROM "UNIT" ORDER BY dbms_random.value) WHERE rownum = 1)
);


-- Employees and projects for many-to-many and reference-to-self testing

INSERT INTO "D_EMPLOYEE" ("ID", "F_NAME", "VERSION") VALUES ('1', 'Oscar Meyer', '1');
INSERT INTO "D_EMPLOYEE" ("ID", "F_NAME", "VERSION") VALUES ('2', 'Steve Smith', '1');
INSERT INTO "D_EMPLOYEE" ("ID", "F_NAME", "VERSION") VALUES ('3', 'John Doe', '2');
INSERT INTO "D_EMPLOYEE" ("ID", "F_NAME", "VERSION") VALUES ('4', 'Marc Miller', '4');
INSERT INTO "D_EMPLOYEE" ("ID", "F_NAME", "VERSION") VALUES ('5', 'Rodney Marx', '2');

INSERT INTO "D_PROJECT" ("ID", "F_NAME", "VERSION") VALUES ('1', 'Project 1', '1');
INSERT INTO "D_PROJECT" ("ID", "F_NAME", "VERSION") VALUES ('2', 'Project 2', '1');

INSERT INTO "LINK_EMPLOYEE_PROJECT" ("LEFT_ID", "RIGHT_ID") VALUES ('1', '1');
--INSERT INTO "LINK_EMPLOYEE_PROJECT" ("LEFT_ID", "RIGHT_ID") VALUES ('2', '1');
--INSERT INTO "LINK_EMPLOYEE_PROJECT" ("LEFT_ID", "RIGHT_ID") VALUES ('3', '1');
INSERT INTO "LINK_EMPLOYEE_PROJECT" ("LEFT_ID", "RIGHT_ID") VALUES ('1', '2');
--INSERT INTO "LINK_EMPLOYEE_PROJECT" ("LEFT_ID", "RIGHT_ID") VALUES ('4', '2');
--INSERT INTO "LINK_EMPLOYEE_PROJECT" ("LEFT_ID", "RIGHT_ID") VALUES ('5', '2');

--INSERT INTO "LINK_EMPLOYEE_EMPLOYEE" ("LEFT_ID", "RIGHT_ID") VALUES ('1', '2');
--INSERT INTO "LINK_EMPLOYEE_EMPLOYEE" ("LEFT_ID", "RIGHT_ID") VALUES ('1', '3');
--INSERT INTO "LINK_EMPLOYEE_EMPLOYEE" ("LEFT_ID", "RIGHT_ID") VALUES ('2', '3');
--INSERT INTO "LINK_EMPLOYEE_EMPLOYEE" ("LEFT_ID", "RIGHT_ID") VALUES ('1', '4');
--INSERT INTO "LINK_EMPLOYEE_EMPLOYEE" ("LEFT_ID", "RIGHT_ID") VALUES ('1', '5');
--INSERT INTO "LINK_EMPLOYEE_EMPLOYEE" ("LEFT_ID", "RIGHT_ID") VALUES ('4', '5');

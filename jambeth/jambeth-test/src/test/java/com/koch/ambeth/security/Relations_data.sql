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
INSERT INTO "Employee" ("ID", "Name", "PRIMARY_ADDRESS_ID", "PRIMARY_PROJECT", "SECONDARY_PROJECT", "CAR_MAKE", "CAR_MODEL", "BOAT", "VERSION") VALUES ('1', 'Oscar Meyer', '11', '21', '22', 'BMW', 'Z1', '31', '1');
INSERT INTO "Employee" ("ID", "Name", "PRIMARY_ADDRESS_ID", "SUPERVISOR", "PRIMARY_PROJECT", "CAR_MAKE", "CAR_MODEL", "BOAT", "VERSION") VALUES ('2', 'Steve Smith', '12', '1', '22', 'VW', 'Golf', '32', '1');
INSERT INTO "Employee" ("ID", "Name", "PRIMARY_ADDRESS_ID", "SUPERVISOR", "PRIMARY_PROJECT", "SECONDARY_PROJECT", "VERSION") VALUES ('3', 'John Doe', '13', '2', '22', '21', '2');

INSERT INTO "ADDRESS" ("ID", "STREET", "CITY", "VERSION") VALUES ('11', 'Main "STREET"', 'Exampletown', '1');
INSERT INTO "ADDRESS" ("ID", "STREET", "CITY", "VERSION") VALUES ('12', 'Second "STREET"', 'Exampletown', '1');
INSERT INTO "ADDRESS" ("ID", "STREET", "CITY", "VERSION") VALUES ('13', 'Main "STREET"', 'Nextown', '1');
INSERT INTO "ADDRESS" ("ID", "RESIDENT", "STREET", "CITY", "VERSION") VALUES ('14', '1', 'Third "STREET"', 'Exampletown', '1');
INSERT INTO "ADDRESS" ("ID", "RESIDENT", "STREET", "CITY", "VERSION") VALUES ('15', '1', 'Backroad', 'Nextown', '1');
INSERT INTO "ADDRESS" ("ID", "STREET", "CITY", "VERSION") VALUES ('16', 'Oldroad', 'Nextown', '1');

INSERT INTO "PROJECT" ("ID", "NAME", "VERSION") VALUES ('21', 'Project 1', '1');
INSERT INTO "PROJECT" ("ID", "NAME", "VERSION") VALUES ('22', 'Project 2', '1');
INSERT INTO "PROJECT" ("ID", "NAME", "VERSION") VALUES ('23', 'Project 3', '1');

INSERT INTO "LINK_EMPLOYEE_PROJECT" ("EMPLOYEE_ID", "PROJECT_ID") VALUES ('1', '22');
INSERT INTO "LINK_EMPLOYEE_PROJECT" ("EMPLOYEE_ID", "PROJECT_ID") VALUES ('1', '21');

INSERT INTO "BOAT" ("ID", "NAME", "VERSION") VALUES ('31', 'Boat 1', '1');
INSERT INTO "BOAT" ("ID", "NAME", "VERSION") VALUES ('32', 'Boat 2', '1');

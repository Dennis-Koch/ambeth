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
INSERT INTO "HOME_ADDRESS" (ID, VERSION, STREAT) VALUES ('1', 1, 'streat_1');
INSERT INTO "HOME_ADDRESS" (ID, VERSION, STREAT) VALUES ('2', 2, 'streat_2');
INSERT INTO "HOME_ADDRESS" (ID, VERSION, STREAT) VALUES ('3', 3, 'streat_3');
INSERT INTO "HOME_ADDRESS" (ID, VERSION, STREAT) VALUES ('4', 4, 'streat_4');


INSERT INTO "ADDRESS" (ID, VERSION, NAME) VALUES ('1', 1, 'addreas_1');
INSERT INTO "ADDRESS" (ID, VERSION, NAME) VALUES ('2', 2, 'addreas_2');
INSERT INTO "ADDRESS" (ID, VERSION, NAME) VALUES ('3', 3, 'addreas_3');
INSERT INTO "ADDRESS" (ID, VERSION, NAME) VALUES ('4', 4, 'addreas_4');



INSERT INTO "HOME" (ID, VERSION, ADDRESS_ID) VALUES ('1', 1, '1');
INSERT INTO "HOME" (ID, VERSION, ADDRESS_ID) VALUES ('2', 2, '2');
INSERT INTO "HOME" (ID, VERSION, ADDRESS_ID) VALUES ('3', 3, '3');
INSERT INTO "HOME" (ID, VERSION, ADDRESS_ID) VALUES ('4', 4, '4');


 
INSERT INTO "PERSON" (ID, VERSION, NAME, AGE, HAVE_ANDRIOD, HAVE_ORANGE, MODIFY_TIME, HOME_ID,HOME_ADDRESS_ID) VALUES (1 , 1 , 'person_name_1', 10, 1, 0, to_timestamp('2016-9-1 1:1:00'  , 'yyyy-mm-dd hh24:mi:ss'), 1, 1);
INSERT INTO "PERSON" (ID, VERSION, NAME, AGE, HAVE_ANDRIOD, HAVE_ORANGE, MODIFY_TIME, HOME_ID,HOME_ADDRESS_ID) VALUES (2 , 1 , 'person_name_2', 12, 0, 1, to_timestamp('2016-9-1 4:4:00'  , 'yyyy-mm-dd hh24:mi:ss'), 1, 1);
INSERT INTO "PERSON" (ID, VERSION, NAME, AGE, HAVE_ANDRIOD, HAVE_ORANGE, MODIFY_TIME, HOME_ID,HOME_ADDRESS_ID) VALUES (3 , 4 , 'person_name_3', 14, 1, 0, to_timestamp('2016-9-2 7:7:00'  , 'yyyy-mm-dd hh24:mi:ss'), 1, 2);
INSERT INTO "PERSON" (ID, VERSION, NAME, AGE, HAVE_ANDRIOD, HAVE_ORANGE, MODIFY_TIME, HOME_ID,HOME_ADDRESS_ID) VALUES (4 , 4 , 'person_name_4', 16, 0, 1, to_timestamp('2016-9-2 10:10:00', 'yyyy-mm-dd hh24:mi:ss'), 2, 2);
INSERT INTO "PERSON" (ID, VERSION, NAME, AGE, HAVE_ANDRIOD, HAVE_ORANGE, MODIFY_TIME, HOME_ID,HOME_ADDRESS_ID) VALUES (5 , 7 , 'person_name_5', 18, 1, 0, to_timestamp('2016-9-3 13:13:00', 'yyyy-mm-dd hh24:mi:ss'), 2, 3);
INSERT INTO "PERSON" (ID, VERSION, NAME, AGE, HAVE_ANDRIOD, HAVE_ORANGE, MODIFY_TIME, HOME_ID,HOME_ADDRESS_ID) VALUES (6 , 7 , 'person_name_6', 20, 0, 1, to_timestamp('2016-9-3 16:16:00', 'yyyy-mm-dd hh24:mi:ss'), 2, 3);
INSERT INTO "PERSON" (ID, VERSION, NAME, AGE, HAVE_ANDRIOD, HAVE_ORANGE, MODIFY_TIME, HOME_ID,HOME_ADDRESS_ID) VALUES (7 , 10, 'person_name_7', 22, 1, 0, to_timestamp('2016-9-4 19:19:00', 'yyyy-mm-dd hh24:mi:ss'), 3, 4);
INSERT INTO "PERSON" (ID, VERSION, NAME, AGE, HAVE_ANDRIOD, HAVE_ORANGE, MODIFY_TIME, HOME_ID,HOME_ADDRESS_ID) VALUES (8 , 10, 'person_name_8', 24, 0, 1, to_timestamp('2016-9-4 22:22:00', 'yyyy-mm-dd hh24:mi:ss'), 3, 4);
INSERT INTO "PERSON" (ID, VERSION, NAME, AGE, HAVE_ANDRIOD, HAVE_ORANGE, MODIFY_TIME, HOME_ID,HOME_ADDRESS_ID) VALUES (9 , 13, 'person_name_11', 26, 1, 0, to_timestamp('2016-9-5 22:25:00', 'yyyy-mm-dd hh24:mi:ss'), 3, 1);
INSERT INTO "PERSON" (ID, VERSION, NAME, AGE, HAVE_ANDRIOD, HAVE_ORANGE, MODIFY_TIME, HOME_ID,HOME_ADDRESS_ID) VALUES (10, 13, NULL            , 28, 0, 1, to_timestamp('2016-9-5 23:28:00', 'yyyy-mm-dd hh24:mi:ss'), 4, 2);



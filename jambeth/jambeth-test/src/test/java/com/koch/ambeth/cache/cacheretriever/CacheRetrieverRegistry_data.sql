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
INSERT INTO LOCAL_ENTITY (ID, NAME, VALUE, PARENT, VERSION) VALUES ('893', 'LocalEntity 893', '15', 'two oh', '1');

INSERT INTO LINK_LOC_ENTITY_EXT_ENTITY (LEFT_ID, RIGHT_ID) VALUES ('893', 'eight nine three');
INSERT INTO LINK_LOC_ENTITY_EXT_ENTITY (LEFT_ID, RIGHT_ID) VALUES ('893', 'one two three four');
INSERT INTO LINK_LOC_ENTITY_EXT_ENTITY (LEFT_ID, RIGHT_ID) VALUES ('893', 'nine');

INSERT INTO LINK_LOCAL_TO_SIBLING (LEFT_ID, RIGHT_ID) VALUES ('893', 'four two');

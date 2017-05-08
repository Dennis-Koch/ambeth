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
INSERT INTO CI_ADMIN.CHILD (ID, VERSION) VALUES ('11', '1');
INSERT INTO CI_ADMIN.CHILD (ID, VERSION) VALUES ('12', '1');

INSERT INTO CI_ADMIN.PARENT (ID, CHILD, VERSION) VALUES ('1', '11', '1');
INSERT INTO CI_ADMIN.PARENT (ID, CHILD, VERSION) VALUES ('2', '12', '1');

INSERT INTO CI_ADMIN_B.CHILD (ID, VERSION) VALUES ('111', '1');
INSERT INTO CI_ADMIN_B.CHILD (ID, VERSION) VALUES ('112', '1');

INSERT INTO CI_ADMIN_B.PARENT (ID, CHILD, VERSION) VALUES ('101', '111', '1');
INSERT INTO CI_ADMIN_B.PARENT (ID, CHILD, VERSION) VALUES ('102', '112', '1');

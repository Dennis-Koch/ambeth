INSERT INTO "PARENT" (ID, NAME, CHILD, VERSION) VALUES ('1', 'Parent Name', '11', '1');
INSERT INTO "PARENT" (ID, NAME, VERSION) VALUES ('2', 'Parent 2 Name', '1');

INSERT INTO "CHILD" (ID, NAME, VERSION) VALUES ('11', 'Child Name', '1');
INSERT INTO "CHILD" (ID, NAME, VERSION) VALUES ('12', 'Child 2 Name', '1');
INSERT INTO "CHILD" (ID, NAME, PARENT, VERSION) VALUES ('13', 'Child 3 Name', '1', '1');
INSERT INTO "CHILD" (ID, NAME, PARENT2, VERSION) VALUES ('14', 'Child 4 Name', '1', '1');

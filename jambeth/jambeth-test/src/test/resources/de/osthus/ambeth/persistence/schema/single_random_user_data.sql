INSERT INTO CHILD (ID, VERSION) VALUES ('11', '1');
INSERT INTO CHILD (ID, VERSION) VALUES ('12', '1');

INSERT INTO PARENT (ID, CHILD, VERSION) VALUES ('1', '11', '1');
INSERT INTO PARENT (ID, CHILD, VERSION) VALUES ('2', '12', '1');
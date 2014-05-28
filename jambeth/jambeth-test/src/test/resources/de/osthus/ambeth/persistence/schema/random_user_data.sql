INSERT INTO ${database.schema.name.0}.CHILD (ID, VERSION) VALUES ('11', '1');
INSERT INTO ${database.schema.name.0}.CHILD (ID, VERSION) VALUES ('12', '1');

INSERT INTO ${database.schema.name.0}.PARENT (ID, CHILD, VERSION) VALUES ('1', '11', '1');
INSERT INTO ${database.schema.name.0}.PARENT (ID, CHILD, VERSION) VALUES ('2', '12', '1');

INSERT INTO ${database.schema.name.1}.CHILD (ID, VERSION) VALUES ('111', '1');
INSERT INTO ${database.schema.name.1}.CHILD (ID, VERSION) VALUES ('112', '1');

INSERT INTO ${database.schema.name.1}.PARENT (ID, CHILD, VERSION) VALUES ('101', '111', '1');
INSERT INTO ${database.schema.name.1}.PARENT (ID, CHILD, VERSION) VALUES ('102', '112', '1');

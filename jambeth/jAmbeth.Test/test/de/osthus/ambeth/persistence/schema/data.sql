INSERT INTO CI_ADMIN.CHILD (ID, VERSION) VALUES ('11', '1');
INSERT INTO CI_ADMIN.CHILD (ID, VERSION) VALUES ('12', '1');

INSERT INTO CI_ADMIN.PARENT (ID, CHILD, VERSION) VALUES ('1', '11', '1');
INSERT INTO CI_ADMIN.PARENT (ID, CHILD, VERSION) VALUES ('2', '12', '1');

INSERT INTO CI_ADMIN_B.CHILD (ID, VERSION) VALUES ('111', '1');
INSERT INTO CI_ADMIN_B.CHILD (ID, VERSION) VALUES ('112', '1');

INSERT INTO CI_ADMIN_B.PARENT (ID, CHILD, VERSION) VALUES ('101', '111', '1');
INSERT INTO CI_ADMIN_B.PARENT (ID, CHILD, VERSION) VALUES ('102', '112', '1');

INSERT INTO "QUERY_ENTITY" (ID, FK, VERSION) VALUES ('1', '2', '2');
INSERT INTO "QUERY_ENTITY" (ID, VERSION) VALUES ('2', '1');
INSERT INTO "QUERY_ENTITY" (ID, FK, VERSION) VALUES ('3', '1', '2');
INSERT INTO "QUERY_ENTITY" (ID, FK, VERSION) VALUES ('4', '2', '1');
INSERT INTO "QUERY_ENTITY" (ID, VERSION) VALUES ('5', '2');

INSERT INTO "JOIN_QUERY_ENTITY" (ID, PARENT, VERSION) VALUES ('1', '2', '2');
INSERT INTO "JOIN_QUERY_ENTITY" (ID, VERSION) VALUES ('2', '3');
INSERT INTO "JOIN_QUERY_ENTITY" (ID, VERSION) VALUES ('3', '1');

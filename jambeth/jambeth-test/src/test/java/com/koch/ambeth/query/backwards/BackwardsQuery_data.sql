INSERT INTO "JOIN_QUERY_ENTITY" (ID, VALUE_FIELD, VERSION) VALUES ('11', '55', '2');
INSERT INTO "JOIN_QUERY_ENTITY" (ID, VALUE_FIELD, PARENT, VERSION) VALUES ('12', '77', '11', '3');

INSERT INTO "QUERY_ENTITY" (ID, NAME, NEXT, JOIN_QUERY_ENTITY, LINK_TABLE_ENTITY, VERSION) VALUES ('1', 'name1', '2', '11', '21', '2');
INSERT INTO "QUERY_ENTITY" (ID, NAME, JOIN_QUERY_ENTITY, VERSION) VALUES ('2', 'name2', '12', '1');

INSERT INTO "LINK_TABLE_ENTITY" (ID, NAME, VERSION) VALUES ('21', 'name21', '1');

INSERT INTO "LINK_JQE_LTE" (LEFT_ID, RIGHT_ID) VALUES ('12', '21');

-- Business object
INSERT INTO "BL_ENTITY_TYPE" (ID, VERSION, NAME, INTERFACES, IS_CLASS) VALUES (1, 1, 'com.koch.ambeth.persistence.blueprint.TestClass',  STRING_ARRAY('com.koch.ambeth.model.IAbstractEntity'), 0);

INSERT INTO "BL_ENTITY_PROPERTY" (ID, VERSION, NAME, TYPE, "ORDER", READONLY)  VALUES (1, 1, 'Something', 'java.lang.String', 0, 0);
INSERT INTO "BL_LINK_ENTITY_TO_ENTITY_PROP" (ENTITY_ID, ENTITY_PROP_ID) VALUES ('1', '1');

INSERT INTO "BL_ENTITY_ANNO" (ID, VERSION, TYPE)  VALUES (1, 1, 'com.koch.ambeth.audit.model.Audited');
INSERT INTO "BL_ENTITY_ANNO_PROPERTY" (ID, VERSION, NAME, VALUE)  VALUES (1, 1, 'value','true');

INSERT INTO "BL_LINK_ENTITY_TO_ANNO" (ENTITY_ID, ANNO_ID) VALUES ('1', '1');
INSERT INTO "BL_LINK_ANNO_TO_ANNO_PROP" (ANNO_ID, ANNO_PROP_ID) VALUES ('1', '1');

-- Value object
INSERT INTO "BL_ENTITY_TYPE" (ID, VERSION, NAME, SUPERCLASS, IS_CLASS) VALUES (2, 2, 'com.koch.ambeth.persistence.blueprint.TestClassV', 'com.koch.ambeth.persistence.blueprint.TransferEntity', 1);

INSERT INTO "BL_ENTITY_PROPERTY" (ID, VERSION, NAME, TYPE, "ORDER", READONLY)  VALUES (2, 2, 'Something', 'java.lang.String', 0, 0);
INSERT INTO "BL_LINK_ENTITY_TO_ENTITY_PROP" (ENTITY_ID, ENTITY_PROP_ID) VALUES ('2', '2');

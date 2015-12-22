INSERT INTO "BL_ENTITY_TYPE" (ID, VERSION, NAME, INHERITS) VALUES (1, 1, 'de.osthus.ambeth.persistence.blueprint.TestClass', STRING_ARRAY('de.osthus.ambeth.model.IAbstractEntity'));

INSERT INTO "BL_ENTITY_PROPERTY" (ID, VERSION, NAME, TYPE, "ORDER", READONLY)  VALUES (1, 1, 'Something', 'java.lang.String', 0, 0);
INSERT INTO "BL_LINK_ENTITY_TO_ENTITY_PROP" (ENTITY_ID, ENTITY_PROP_ID) VALUES ('1', '1');

INSERT INTO "BL_ENTITY_TYPE" (ID, VERSION, NAME, INHERITS) VALUES (2, 2, 'de.osthus.ambeth.persistence.blueprint.TestClassV', STRING_ARRAY('de.osthus.ambeth.model.IAbstractEntity'));

INSERT INTO "BL_ENTITY_PROPERTY" (ID, VERSION, NAME, TYPE, "ORDER", READONLY)  VALUES (2, 2, 'Something', 'java.lang.String', 0, 0);
INSERT INTO "BL_LINK_ENTITY_TO_ENTITY_PROP" (ENTITY_ID, ENTITY_PROP_ID) VALUES ('2', '2');

CREATE TABLE "QUERY_ENTITY"
  (
    "ID"   NUMBER NOT NULL,
    "FK"   NUMBER NULL,
    "NAME1" VARCHAR2(30 CHAR),
    "NAME2" VARCHAR2(30 CHAR),
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 CHAR),
    "CREATED_BY" VARCHAR2(16 CHAR),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "PK_QUERY_ENTITY" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "LINK_QE_JQE_FK1" FOREIGN KEY ("FK") REFERENCES "JOIN_QUERY_ENTITY" ("ID") DEFERRABLE INITIALLY DEFERRED
);
CREATE SEQUENCE "QUERY_ENTITY_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE;
CREATE INDEX "IX_QE_JQE_FK1" ON "QUERY_ENTITY" ("FK");

CREATE INDEX "IX_QE_VERSION" ON "QUERY_ENTITY" ("ID","VERSION");

CREATE TABLE "JOIN_QUERY_ENTITY"
  (
    "ID"   NUMBER NOT NULL,
    "PARENT"   NUMBER NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 CHAR),
    "CREATED_BY" VARCHAR2(16 CHAR),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "PK_JOIN_QUERY_ENTITY" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "LINK_JQE_JQE_FK1" FOREIGN KEY ("PARENT") REFERENCES "JOIN_QUERY_ENTITY" ("ID") DEFERRABLE INITIALLY DEFERRED
);
CREATE SEQUENCE "JOIN_QUERY_ENTITY_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE;
CREATE INDEX "IX_JQE_JQE_FK1" ON "JOIN_QUERY_ENTITY" ("PARENT");

CREATE TABLE "EMPLOYEE"
  (
    "ID"   NUMBER NOT NULL,
    "NAME" VARCHAR2(50 CHAR) NOT NULL,
    "PRIMARY_ADDRESS" NUMBER NOT NULL,
    "SUPERVISOR"   NUMBER NULL,
    "PRIMARY_PROJECT" NUMBER NOT NULL,
    "SECONDARY_PROJECT" NUMBER NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 CHAR),
    "CREATED_BY" VARCHAR2(16 CHAR),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "EMPLOYEE_PK" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "EMPLOYEE_AE_NAME" UNIQUE ("NAME"),
    CONSTRAINT "LINK_EMPLOYEE_PRIM_ADDR_FK1" FOREIGN KEY ("PRIMARY_ADDRESS") REFERENCES "ADDRESS" ("ID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_EMPLOYEE_EMPLOYEE_FK1" FOREIGN KEY ("SUPERVISOR") REFERENCES "EMPLOYEE" ("ID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_EMPLOYEE_PROJECT_FK1" FOREIGN KEY ("PRIMARY_PROJECT") REFERENCES "PROJECT" ("ID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_EMPLOYEE_PROJECT_FK2" FOREIGN KEY ("SECONDARY_PROJECT") REFERENCES "PROJECT" ("ID") DEFERRABLE INITIALLY DEFERRED
  );
CREATE SEQUENCE "EMPLOYEE_SEQU"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;
CREATE INDEX "IX_EMPLOYEE_PRIM_ADDR_FK1" ON "EMPLOYEE" ("PRIMARY_ADDRESS");
CREATE INDEX "IX_EMPLOYEE_EMPLOYEE_FK1" ON "EMPLOYEE" ("SUPERVISOR");
CREATE INDEX "IX_EMPLOYEE_PROJECT_FK1" ON "EMPLOYEE" ("PRIMARY_PROJECT");
CREATE INDEX "IX_EMPLOYEE_PROJECT_FK2" ON "EMPLOYEE" ("SECONDARY_PROJECT");

CREATE TABLE "ADDRESS"
  (
    "ID"   NUMBER NOT NULL,
    "EMPLOYEE"   NUMBER NULL,
    "STREET" VARCHAR2(50 CHAR) NOT NULL,
    "CITY" VARCHAR2(50 CHAR) NOT NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 CHAR),
    "CREATED_BY" VARCHAR2(16 CHAR),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "ADDRESS_PK" PRIMARY KEY ("ID") USING INDEX
  );
CREATE SEQUENCE "ADDRESS_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

ALTER TABLE "ADDRESS"
    ADD CONSTRAINT "LINK_ADDRESS_EMPLOYEE_FK1" FOREIGN KEY ("EMPLOYEE") REFERENCES "EMPLOYEE" ("ID") DEFERRABLE INITIALLY DEFERRED;
CREATE INDEX "IX_ADDRESS_EMPLOYEE_FK1" ON "ADDRESS" ("EMPLOYEE");

CREATE TABLE "PROJECT"
  (
    "ID"   NUMBER NOT NULL,
    "NAME" VARCHAR2(50 CHAR) NOT NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 CHAR),
    "CREATED_BY" VARCHAR2(16 CHAR),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "PROJECT_PK" PRIMARY KEY ("ID") USING INDEX
  );
CREATE SEQUENCE "PROJECT_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "LINK_EMPLOYEE_PROJECT"
  (
    "LEFT_ID"  NUMBER NOT NULL,
    "RIGHT_ID" NUMBER NOT NULL,
    CONSTRAINT "LINK_EMPLOYEE_PROJECT_PK" PRIMARY KEY ("LEFT_ID", "RIGHT_ID") USING INDEX,
    CONSTRAINT "LINK_EMPLOYEE_PROJECT_FK3" FOREIGN KEY ("LEFT_ID") REFERENCES "EMPLOYEE" ("ID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_EMPLOYEE_PROJECT_FK4" FOREIGN KEY ("RIGHT_ID") REFERENCES "PROJECT" ("ID") DEFERRABLE INITIALLY DEFERRED
  );
CREATE INDEX "IX_EMPLOYEE_PROJECT_FK4" ON "LINK_EMPLOYEE_PROJECT" ("RIGHT_ID");

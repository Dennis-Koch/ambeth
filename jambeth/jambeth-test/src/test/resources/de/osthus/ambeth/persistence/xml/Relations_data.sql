INSERT INTO "EMPLOYEE" (ID, NAME, PRIMARY_ADDRESS_ID, PRIMARY_PROJECT, SECONDARY_PROJECT, CAR_MAKE, CAR_MODEL, BOAT, VERSION) VALUES ('1', 'Oscar Meyer', '11', '21', '22', 'BMW', 'Z1', '31', '1');
INSERT INTO "EMPLOYEE" (ID, NAME, PRIMARY_ADDRESS_ID, SUPERVISOR, PRIMARY_PROJECT, CAR_MAKE, CAR_MODEL, BOAT, VERSION) VALUES ('2', 'Steve Smith', '12', '1', '22', 'VW', 'Golf', '32', '1');
INSERT INTO "EMPLOYEE" (ID, NAME, PRIMARY_ADDRESS_ID, SUPERVISOR, PRIMARY_PROJECT, SECONDARY_PROJECT, VERSION) VALUES ('3', 'John Doe', '13', '2', '22', '21', '2');

INSERT INTO "ADDRESS" (ID, STREET, CITY, VERSION) VALUES ('11', 'Main Street', 'Exampletown', '1');
INSERT INTO "ADDRESS" (ID, STREET, CITY, VERSION) VALUES ('12', 'Second Street', 'Exampletown', '1');
INSERT INTO "ADDRESS" (ID, STREET, CITY, VERSION) VALUES ('13', 'Main Street', 'Nextown', '1');
INSERT INTO "ADDRESS" (ID, RESIDENT, STREET, CITY, VERSION) VALUES ('14', '1', 'Third Street', 'Exampletown', '1');
INSERT INTO "ADDRESS" (ID, RESIDENT, STREET, CITY, VERSION) VALUES ('15', '1', 'Backroad', 'Nextown', '1');
INSERT INTO "ADDRESS" (ID, STREET, CITY, VERSION) VALUES ('16', 'Oldroad', 'Nextown', '1');

INSERT INTO "PROJECT" (ID, NAME, VERSION) VALUES ('21', 'Project 1', '1');
INSERT INTO "PROJECT" (ID, NAME, VERSION) VALUES ('22', 'Project 2', '1');
INSERT INTO "PROJECT" (ID, NAME, VERSION) VALUES ('23', 'Project 3', '1');

INSERT INTO "LINK_EMPLOYEE_PROJECT" (LEFT_ID, RIGHT_ID) VALUES ('1', '22');
INSERT INTO "LINK_EMPLOYEE_PROJECT" (LEFT_ID, RIGHT_ID) VALUES ('1', '21');

INSERT INTO "BOAT" (ID, NAME, VERSION) VALUES ('31', 'Boat 1', '1');
INSERT INTO "BOAT" (ID, NAME, VERSION) VALUES ('32', 'Boat 2', '1');
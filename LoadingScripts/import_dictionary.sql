CREATE TABLE bnode_values 	(id integer NOT NULL, "value" character varying(127) NOT NULL) WITH (OIDS=FALSE); 
CREATE TABLE datatype_values 	(id integer NOT NULL, "value" character varying(255) NOT NULL) WITH (OIDS=FALSE); 
CREATE TABLE datetime_values	(id integer NOT NULL, "value" bigint NOT NULL) WITH (OIDS=FALSE); 
CREATE TABLE hash_values 	(id integer NOT NULL, "value" bigint NOT NULL) WITH (OIDS=FALSE); 
CREATE TABLE label_values 	(id integer NOT NULL, "value" character varying(255) NOT NULL) WITH (OIDS=FALSE); 
CREATE TABLE language_values 	(id integer NOT NULL, "value" character varying(127) NOT NULL) WITH (OIDS=FALSE); 
CREATE TABLE long_label_values 	(id integer NOT NULL, "value" text NOT NULL) WITH (OIDS=FALSE); 
CREATE TABLE long_uri_values 	(id integer NOT NULL, "value" text NOT NULL) WITH (OIDS=FALSE); 
CREATE TABLE numeric_values 	(id integer NOT NULL, "value" double precision NOT NULL) WITH (OIDS=FALSE); 
CREATE TABLE uri_values 	(id integer NOT NULL, "value" character varying(255) NOT NULL) WITH (OIDS=FALSE); 

--
-- NOTICE: if the NULL string below is changed, then macro CSV_NULL_STR has to be updated accordingly
--
\copy label_values 	FROM 'PWDPATH/labels.csv' 	CSV NULL 'LAFABRICNEVERSTOPS@BRAHAMES' ESCAPE '\';
\copy uri_values 	FROM 'PWDPATH/uris.csv' 	CSV ESCAPE '\';
\copy long_uri_values 	FROM 'PWDPATH/long_uris.csv' 	CSV ESCAPE '\';
\copy bnode_values 	FROM 'PWDPATH/bnodes.csv' 	CSV ESCAPE '\';
\copy hash_values 	FROM 'PWDPATH/hash.csv' 	CSV ESCAPE '\';
\copy numeric_values	FROM 'PWDPATH/numerics.csv' 	CSV NULL 'LAFABRICNEVERSTOPS@BRAHAMES' ESCAPE '\';
\copy datetime_values 	FROM 'PWDPATH/datetime.csv' 	CSV NULL 'LAFABRICNEVERSTOPS@BRAHAMES' ESCAPE '\';
\copy long_label_values  FROM 'PWDPATH/long_labels.csv' 	CSV ESCAPE '\';
\copy datatype_values 	FROM 'PWDPATH/datatypes.csv' 	CSV NULL 'LAFABRICNEVERSTOPS@BRAHAMES' ESCAPE '\';
\copy language_values 	FROM 'PWDPATH/languages.csv' 	CSV ESCAPE '\'; 

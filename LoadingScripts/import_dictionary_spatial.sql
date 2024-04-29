CREATE TABLE geo_values (id integer NOT NULL, strdfgeo geometry, srid integer NOT NULL) WITH (OIDS=FALSE);
COPY geo_values FROM 'PWDPATH/geo_values.csv' CSV;

SELECT Populate_Geometry_Columns();
SELECT UpdateGeometrySRID('geo_values', 'strdfgeo', 4326);

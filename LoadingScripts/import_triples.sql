CREATE TABLE triples(ctx integer NOT NULL, subj integer NOT NULL, pred integer NOT NULL, obj integer NOT NULL, expl boolean NOT NULL) WITH (OIDS=FALSE);
--ALTER TABLE triples OWNER TO strabon;
COPY triples FROM 'PWDPATH/triples.csv' using delimiters ',';

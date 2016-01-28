CREATE OR REPLACE VIEW lastgeodatas AS 
 SELECT g1.id, g1.partnerid, g1.uuid, g1.lat, g1.lon, g1.direction, g1.speed, 
    g1.category, g1.date
   FROM geodatas g1
   JOIN ( SELECT geodatas.partnerid, geodatas.uuid, 
            max(geodatas.date) AS maxdate
           FROM geodatas
          GROUP BY geodatas.partnerid, geodatas.uuid) g2 ON g1.partnerid = g2.partnerid AND g1.uuid::text = g2.uuid::text AND g1.date = g2.maxdate;

ALTER TABLE lastgeodatas
  OWNER TO btaxi;


CREATE OR REPLACE FUNCTION geodatas_delete_old()
  RETURNS trigger AS
$BODY$
BEGIN
  DELETE FROM geodatas WHERE date < NOW() - INTERVAL '1 hour';
  RETURN NULL;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION geodatas_delete_old()
  OWNER TO btaxi;

CREATE TRIGGER geodatas_tr1
  AFTER INSERT
  ON geodatas
  FOR EACH STATEMENT
  EXECUTE PROCEDURE geodatas_delete_old();


 CREATE INDEX geodatas_idx1
  ON geodatas
  USING btree
  (partnerid, uuid COLLATE pg_catalog."default");
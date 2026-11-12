-- Questo script viene eseguito automaticamente al primo avvio del container vessel-db
-- Abilita l'estensione PostGIS sul database vessel_db

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- Verifica che PostGIS sia attivo
SELECT PostGIS_Full_Version();
DELETE FROM bankaccount WHERE id BETWEEN 1 AND 150;
DELETE FROM payment WHERE fromaccount BETWEEN 1 AND 150;
DELETE FROM payment WHERE toaccount BETWEEN 1 AND 150;
INSERT INTO bankaccount(id, number, username)
  SELECT x.id, md5(x.id::text), x.id FROM generate_series(1,150) AS x(id);
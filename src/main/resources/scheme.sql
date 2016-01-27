CREATE TABLE progress
(
  id INT PRIMARY KEY IDENTITY,
  peer VARCHAR(50) NOT NULL,
  piece_id INT NOT NULL,
  chunk_length INT,
  chunk_offset INT
);
CREATE UNIQUE INDEX "promisegress_id_uindex" ON progress (id);

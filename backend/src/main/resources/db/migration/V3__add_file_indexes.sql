-- Accélère la recherche des fichiers par propriétaire.
CREATE INDEX idx_files_owner_id
    ON files (owner_id);

-- Accélère la recherche des fichiers expirés lors de la purge.
CREATE INDEX idx_files_expires_at
    ON files (expires_at);

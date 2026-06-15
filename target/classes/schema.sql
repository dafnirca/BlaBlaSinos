CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    senha TEXT NOT NULL,
    tipo TEXT NOT NULL,
    tentativas_falhas INTEGER NOT NULL DEFAULT 0,
    bloqueado_ate INTEGER,
    cnh TEXT,
    marca_veiculo TEXT,
    modelo_veiculo TEXT,
    cor_veiculo TEXT,
    placa_veiculo TEXT
    ,vagas INTEGER
);

CREATE TABLE IF NOT EXISTS caronas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    motorista_id INTEGER NOT NULL,
    origem TEXT NOT NULL,
    destino TEXT NOT NULL,
    horario_saida TEXT NOT NULL,
    vagas_total INTEGER NOT NULL,
    vagas_disponiveis INTEGER NOT NULL,
    valor REAL NOT NULL DEFAULT 0,
    observacoes TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'AGENDADA',
    criado_em TEXT NOT NULL,
    FOREIGN KEY (motorista_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS reservas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    carona_id INTEGER NOT NULL,
    passageiro_id INTEGER NOT NULL,
    status TEXT NOT NULL, -- PENDENTE, CONFIRMADA, CANCELADA
    FOREIGN KEY (carona_id) REFERENCES caronas (id),
    FOREIGN KEY (passageiro_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS notificacoes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER NOT NULL,
    tipo TEXT NOT NULL,
    mensagem TEXT NOT NULL,
    referencia_id INTEGER,
    lida INTEGER NOT NULL DEFAULT 0,
    criada_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS avaliacoes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    carona_id INTEGER NOT NULL,
    avaliador_id INTEGER NOT NULL,
    avaliado_id INTEGER NOT NULL,
    nota INTEGER NOT NULL,
    comentario TEXT DEFAULT '',
    data_avaliacao TEXT NOT NULL,
    UNIQUE(carona_id, avaliador_id, avaliado_id),
    FOREIGN KEY (carona_id) REFERENCES caronas (id),
    FOREIGN KEY (avaliador_id) REFERENCES usuarios (id),
    FOREIGN KEY (avaliado_id) REFERENCES usuarios (id)
);

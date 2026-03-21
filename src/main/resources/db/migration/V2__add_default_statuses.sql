INSERT INTO statuses (name, color) VALUES ('В процессе', '#FFA500')
ON CONFLICT (name) DO NOTHING;

INSERT INTO statuses (name, color) VALUES ('Готово', '#008000')
ON CONFLICT (name) DO NOTHING;

INSERT INTO statuses (name, color) VALUES ('Ошибка', '#FF0000')
ON CONFLICT (name) DO NOTHING;
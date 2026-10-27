-- Test kullanıcıları: bir QA veritabanındaki kullanıcı tablosunu taklit eder.
-- Kaynak: saucedemo.com giriş sayfasında listelenen herkese açık demo kullanıcıları.
-- Şifre burada TUTULMAZ: {{password}} test çalışırken TestSecrets ile çözülür.
CREATE TABLE users (
    username       VARCHAR(50)  PRIMARY KEY,
    password       VARCHAR(50)  NOT NULL,
    user_type      VARCHAR(30)  NOT NULL,
    can_login      BOOLEAN      NOT NULL,
    expected_error VARCHAR(200),
    active         BOOLEAN      NOT NULL
);

INSERT INTO users VALUES ('standard_user',           '{{password}}', 'standard',    TRUE,  NULL, TRUE);
INSERT INTO users VALUES ('problem_user',            '{{password}}', 'buggy UI',    TRUE,  NULL, TRUE);
INSERT INTO users VALUES ('performance_glitch_user', '{{password}}', 'slow',        TRUE,  NULL, TRUE);
INSERT INTO users VALUES ('error_user',              '{{password}}', 'error-prone', TRUE,  NULL, TRUE);
INSERT INTO users VALUES ('visual_user',             '{{password}}', 'visual bugs', TRUE,  NULL, TRUE);
INSERT INTO users VALUES ('locked_out_user',         '{{password}}', 'locked',      FALSE, 'Epic sadface: Sorry, this user has been locked out.', TRUE);
-- Pasif kayıt: sorgu (WHERE active = TRUE) bunu hiç getirmemeli
INSERT INTO users VALUES ('retired_user',            '{{password}}', 'retired',     FALSE, NULL, FALSE);

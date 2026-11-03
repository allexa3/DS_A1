--
-- Credential database (auth-service). Schema only: credentials are created at runtime by the User service,
-- including the bootstrap administrator. Regenerate a full dump with docs/export-db-dumps.sh.
--

CREATE TABLE public.credentials (
    user_id uuid NOT NULL,
    password_hash character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    CONSTRAINT credentials_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'CLIENT'::character varying])::text[]))),
    CONSTRAINT credentials_pkey PRIMARY KEY (user_id),
    CONSTRAINT credentials_username_key UNIQUE (username)
);

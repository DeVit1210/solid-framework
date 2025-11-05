-- 01-init-outbox.sql
DROP TABLE IF EXISTS public.outbox_events;
DROP TABLE IF EXISTS public.orders;

CREATE TABLE public.outbox_events (
    id            BIGSERIAL PRIMARY KEY,
    aggregatetype TEXT NOT NULL,
    aggregateid   TEXT NOT NULL,
    type          TEXT NOT NULL,
    payload       JSONB NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

ALTER TABLE public.outbox_events REPLICA IDENTITY FULL;

CREATE INDEX idx_outbox_aggregatetype ON public.outbox_events(aggregatetype);
CREATE INDEX idx_outbox_created_at   ON public.outbox_events(created_at);

-- Demo aggregate
CREATE TABLE public.orders (
    id         TEXT PRIMARY KEY,
    customer   TEXT,
    total      DECIMAL(10,2),
    status     TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
-- SONIQ Phase 3 Migration: Authorized Music Catalogue Metadata & Read-Only RLS
-- Stores metadata and authorized stream/artwork URLs only (NEVER binary audio in PostgreSQL).
-- Public catalogue tables are strictly read-only for anon and authenticated clients.

create table if not exists public.genres (
    id text primary key,
    name text not null unique,
    mood_tag text not null default '',
    accent_hex text not null default '#9D4EDD',
    sort_order integer not null default 0,
    created_at timestamptz not null default timezone('utc'::text, now())
);

create table if not exists public.artists (
    id text primary key,
    name text not null,
    bio text not null default '',
    artwork_url text,
    primary_genre_id text references public.genres (id) on delete set null,
    is_featured boolean not null default false,
    created_at timestamptz not null default timezone('utc'::text, now()),
    constraint artists_artwork_https check (artwork_url is null or artwork_url = '' or artwork_url ~* '^https://')
);

create table if not exists public.albums (
    id text primary key,
    title text not null,
    artist_id text not null references public.artists (id) on delete cascade,
    genre_id text references public.genres (id) on delete set null,
    release_year integer,
    artwork_url text,
    is_featured boolean not null default false,
    created_at timestamptz not null default timezone('utc'::text, now()),
    constraint albums_artwork_https check (artwork_url is null or artwork_url = '' or artwork_url ~* '^https://')
);

create table if not exists public.tracks (
    id text primary key,
    title text not null,
    artist_id text not null references public.artists (id) on delete cascade,
    album_id text references public.albums (id) on delete set null,
    genre_id text references public.genres (id) on delete set null,
    duration_ms bigint not null check (duration_ms >= 0),
    track_number integer not null default 1 check (track_number > 0),
    stream_url text not null,
    artwork_url text,
    license_name text not null,
    license_url text not null,
    is_published boolean not null default true,
    created_at timestamptz not null default timezone('utc'::text, now()),
    constraint tracks_stream_https check (stream_url ~* '^https://'),
    constraint tracks_artwork_https check (artwork_url is null or artwork_url = '' or artwork_url ~* '^https://'),
    constraint tracks_license_https check (license_url ~* '^https://')
);

create index if not exists idx_tracks_genre_id on public.tracks (genre_id);
create index if not exists idx_tracks_artist_id on public.tracks (artist_id);
create index if not exists idx_tracks_album_id on public.tracks (album_id);
create index if not exists idx_albums_artist_id on public.albums (artist_id);

-- Strictly enable Row Level Security on all catalogue tables
alter table public.genres enable row level security;
alter table public.artists enable row level security;
alter table public.albums enable row level security;
alter table public.tracks enable row level security;

-- Revoke all table-level privileges and grant strictly read-only SELECT to clients
revoke all privileges on table public.genres, public.artists, public.albums, public.tracks
    from public, anon, authenticated;

grant select on table public.genres, public.artists, public.albums, public.tracks
    to anon, authenticated;

-- Read-only RLS policies for public catalogue metadata
drop policy if exists "genres_select_public" on public.genres;
create policy "genres_select_public"
    on public.genres
    for select
    to anon, authenticated
    using (true);

drop policy if exists "artists_select_public" on public.artists;
create policy "artists_select_public"
    on public.artists
    for select
    to anon, authenticated
    using (true);

drop policy if exists "albums_select_public" on public.albums;
create policy "albums_select_public"
    on public.albums
    for select
    to anon, authenticated
    using (true);

drop policy if exists "tracks_select_published" on public.tracks;
create policy "tracks_select_published"
    on public.tracks
    for select
    to anon, authenticated
    using (is_published = true);

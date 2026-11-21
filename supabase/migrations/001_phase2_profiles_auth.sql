-- SONIQ Phase 2 Migration (Final Security Hardened):
-- User Profiles, Case-Insensitive Username Uniqueness, Auth-Managed Email Integrity,
-- Server-Controlled Profile Provisioning & Row Level Security.
-- Execute in Supabase SQL Editor or via Supabase CLI migrations.

create table if not exists public.profiles (
    id uuid primary key references auth.users (id) on delete cascade,
    email text not null,
    username text,
    display_name text not null default '',
    avatar_url text,
    bio text not null default '',
    created_at timestamptz not null default timezone('utc'::text, now()),
    updated_at timestamptz not null default timezone('utc'::text, now()),
    constraint profiles_display_name_len check (char_length(display_name) <= 40),
    constraint profiles_username_len check (username is null or (char_length(username) between 3 and 24)),
    constraint profiles_username_format check (username is null or username ~ '^[a-z0-9._]{3,24}$'),
    constraint profiles_bio_len check (char_length(bio) <= 160),
    constraint profiles_avatar_https check (avatar_url is null or avatar_url = '' or avatar_url ~* '^https://')
);

-- Drop legacy case-sensitive constraint if upgrading an existing database
alter table public.profiles drop constraint if exists profiles_username_key;

-- Enforce case-insensitive username uniqueness at the PostgreSQL level
-- ("Shifin", "shifin", and "SHIFIN" resolve to the same normalized key)
create unique index if not exists idx_profiles_username_lower_unique
    on public.profiles (lower(btrim(username)))
    where username is not null and btrim(username) <> '';

-- Strictly enable Row Level Security
alter table public.profiles enable row level security;

-- Revoke direct client-side INSERT and DELETE on public.profiles.
-- Profile rows are provisioned exclusively by server-side SECURITY DEFINER functions
-- (the auth.users signup trigger and ensure_own_profile RPC), guaranteeing clients
-- can never insert a profile with another user's email or forged identity fields.
revoke all privileges on table public.profiles
    from public, anon, authenticated;

revoke all privileges (id, email, username, display_name, avatar_url, bio, created_at, updated_at)
    on table public.profiles
    from public, anon, authenticated;

grant select on table public.profiles to authenticated;

grant update (username, display_name, avatar_url, bio)
    on table public.profiles to authenticated;

-- Drop any legacy client INSERT policy
drop policy if exists "profiles_insert_own" on public.profiles;

-- Users can read only their own profile row
drop policy if exists "profiles_select_own" on public.profiles;
create policy "profiles_select_own"
    on public.profiles
    for select
    to authenticated
    using (auth.uid() = id);

-- Users can update only their own profile row (and only permitted columns above)
drop policy if exists "profiles_update_own" on public.profiles;
create policy "profiles_update_own"
    on public.profiles
    for update
    to authenticated
    using (auth.uid() = id)
    with check (auth.uid() = id);

-- Normalize username to lowercase and block client-side overwrites of Auth-managed email
create or replace function public.enforce_profile_integrity()
returns trigger
language plpgsql
as $$
begin
    -- Normalize username consistently to lowercase (blank -> null)
    if new.username is not null then
        new.username := nullif(lower(btrim(new.username)), '');
    end if;

    -- Defense-in-depth: prevent ordinary client updates from modifying Auth-managed email
    if tg_op = 'UPDATE'
        and new.email is distinct from old.email
        and current_user in ('authenticated', 'anon') then
        raise exception 'Profile email is managed by Supabase Auth and cannot be modified via profile updates.'
            using errcode = '42501';
    end if;

    if tg_op = 'UPDATE' then
        new.updated_at = timezone('utc'::text, now());
    end if;

    return new;
end;
$$;

drop trigger if exists trg_profiles_updated_at on public.profiles;
drop trigger if exists trg_profiles_integrity on public.profiles;
create trigger trg_profiles_integrity
    before insert or update on public.profiles
    for each row
    execute function public.enforce_profile_integrity();

-- Automatically provision a profile row on auth.users signup (works with RLS enabled)
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    initial_name text;
begin
    initial_name := coalesce(
        nullif(trim(new.raw_user_meta_data ->> 'display_name'), ''),
        split_part(coalesce(new.email, 'listener'), '@', 1)
    );

    insert into public.profiles (id, email, display_name)
    values (
        new.id,
        coalesce(new.email, ''),
        left(initial_name, 40)
    )
    on conflict (id) do nothing;

    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row
    execute function public.handle_new_user();

-- Idempotent server-side fallback RPC for authenticated users whose account existed
-- prior to migration deployment. Reads authoritative id/email/display_name strictly
-- from auth.users for auth.uid() and never accepts client-supplied email.
create or replace function public.ensure_own_profile()
returns public.profiles
language plpgsql
security definer
set search_path = public
as $$
declare
    caller_id uuid := auth.uid();
    auth_email text;
    initial_name text;
    result_row public.profiles;
begin
    if caller_id is null then
        raise exception 'Authentication required' using errcode = '42501';
    end if;

    select
        coalesce(u.email, ''),
        coalesce(
            nullif(trim(u.raw_user_meta_data ->> 'display_name'), ''),
            split_part(coalesce(u.email, 'listener'), '@', 1)
        )
    into auth_email, initial_name
    from auth.users u
    where u.id = caller_id;

    if not found then
        raise exception 'Authenticated user record not found' using errcode = 'P0002';
    end if;

    insert into public.profiles (id, email, display_name)
    values (caller_id, auth_email, left(initial_name, 40))
    on conflict (id) do nothing;

    select * into result_row
    from public.profiles
    where id = caller_id;

    return result_row;
end;
$$;

revoke all on function public.ensure_own_profile() from public, anon;
grant execute on function public.ensure_own_profile() to authenticated;

-- Synchronize public.profiles.email whenever Supabase Auth updates auth.users.email
create or replace function public.handle_auth_user_email_updated()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if new.email is distinct from old.email then
        update public.profiles
        set email = coalesce(new.email, ''),
            updated_at = timezone('utc'::text, now())
        where id = new.id;
    end if;

    return new;
end;
$$;

drop trigger if exists on_auth_user_email_updated on auth.users;
create trigger on_auth_user_email_updated
    after update of email on auth.users
    for each row
    execute function public.handle_auth_user_email_updated();

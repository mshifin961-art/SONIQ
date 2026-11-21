-- SONIQ Phase 2 Forward Migration: Explicit Column-Level Privilege Hardening on public.profiles
-- Safe and idempotent for databases where 002_phase2_profiles_privilege_hardening.sql
-- has already been applied. Preserves all profile data, triggers, SECURITY DEFINER functions,
-- case-insensitive username uniqueness, and RLS policies.

-- 1. Revoke all table-level privileges from PUBLIC, anon, and authenticated
revoke all privileges on table public.profiles
    from public, anon, authenticated;

-- 2. Explicitly revoke any pre-existing column-level privileges across all columns of public.profiles
revoke all privileges (id, email, username, display_name, avatar_url, bio, created_at, updated_at)
    on table public.profiles
    from public, anon, authenticated;

-- 3. Grant strictly minimal permissions to authenticated users:
--    - SELECT (row visibility enforced by RLS policy "profiles_select_own": auth.uid() = id)
--    - Column-level UPDATE strictly limited to username, display_name, avatar_url, and bio
grant select on table public.profiles to authenticated;

grant update (username, display_name, avatar_url, bio)
    on table public.profiles to authenticated;

-- 4. Keep RLS enabled and legacy client INSERT policy removed
alter table public.profiles enable row level security;
drop policy if exists "profiles_insert_own" on public.profiles;

-- 5. Keep execute permissions restricted on server-side profile provisioning RPC
revoke all on function public.ensure_own_profile() from public, anon;
grant execute on function public.ensure_own_profile() to authenticated;

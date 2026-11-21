-- SONIQ Phase 2 Forward Migration: PostgreSQL Privilege Hardening on public.profiles
-- Safe and idempotent for databases where 001_phase2_profiles_auth.sql was already applied.
-- Preserves all existing profile rows, triggers, SECURITY DEFINER functions, indexes, and RLS policies.

-- 1. Revoke all pre-existing table-level and column-level privileges from PUBLIC, anon, and authenticated.
revoke all privileges on table public.profiles
    from public, anon, authenticated;

revoke all privileges (id, email, username, display_name, avatar_url, bio, created_at, updated_at)
    on table public.profiles
    from public, anon, authenticated;

-- 2. Grant strictly minimal privileges to authenticated users:
--    - SELECT (row visibility enforced by RLS policy "profiles_select_own": auth.uid() = id)
--    - Column-level UPDATE strictly limited to mutable profile fields
grant select on table public.profiles to authenticated;

grant update (username, display_name, avatar_url, bio)
    on table public.profiles to authenticated;

-- 3. Ensure legacy client-side INSERT policy remains removed and RLS remains enabled
alter table public.profiles enable row level security;
drop policy if exists "profiles_insert_own" on public.profiles;

-- 4. Re-verify execute privileges on server-side profile provisioning RPC
--    (handle_new_user, ensure_own_profile, and handle_auth_user_email_updated are SECURITY DEFINER
--     owned by the migration role and continue operating with RLS enabled and client privileges revoked.)
revoke all on function public.ensure_own_profile() from public, anon;
grant execute on function public.ensure_own_profile() to authenticated;

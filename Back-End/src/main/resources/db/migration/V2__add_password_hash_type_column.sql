-- Add column to track password hash type for migration from MD5 to BCrypt
-- This column helps distinguish between legacy MD5 hashes and new BCrypt hashes

ALTER TABLE t_control_user
ADD COLUMN IF NOT EXISTS password_hash_type VARCHAR(10) DEFAULT 'MD5';

-- Update existing BCrypt hashes (if any) to have correct type
-- BCrypt hashes start with $2a$, $2b$, or $2y$
UPDATE t_control_user
SET password_hash_type = 'BCRYPT'
WHERE password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%';

-- Add comment for documentation
COMMENT ON COLUMN t_control_user.password_hash_type IS 'Password hash algorithm: MD5 (legacy) or BCRYPT (current)';

-- =============================================================================
-- DATABASE ALIGNMENT FIXES - Run this to synchronize database with models
-- =============================================================================

-- ✅ STEP 1: Remove redundant `item_taxes` table (using `import_item_taxes` instead)
DROP TABLE IF EXISTS `item_taxes`;

-- ✅ STEP 2: Verify `import_item_taxes` junction table exists with correct structure
-- This table should already exist, but verify it:
-- CREATE TABLE `import_item_taxes` (
--   `item_id` bigint(20) NOT NULL,
--   `tax_id` bigint(20) NOT NULL,
--   PRIMARY KEY (`item_id`,`tax_id`),
--   KEY `tax_id` (`tax_id`),
--   CONSTRAINT `import_item_taxes_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `import_items` (`item_id`) ON DELETE CASCADE,
--   CONSTRAINT `import_item_taxes_ibfk_2` FOREIGN KEY (`tax_id`) REFERENCES `taxes` (`tax_id`) ON DELETE CASCADE
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ✅ STEP 3: Update all seeds in `users` table to have proper status values
UPDATE `users` SET `status` = 'ACTIVE' WHERE `status` IS NULL OR `status` = '';

-- Verify all users have status:
SELECT `user_id`, `username`, `role`, `status` FROM `users`;

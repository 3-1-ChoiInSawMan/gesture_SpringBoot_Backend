-- ============================================================
-- quick_slot_presets 테이블 마이그레이션
--   변경 내용:
--   1. action_id_1 ~ action_id_5 (quick_actions FK) 제거
--   2. slot_id_1 ~ slot_id_5 (quick_slots FK) 추가
--   3. updated_at 컬럼 추가
-- ============================================================

-- Hibernate 자동 생성 FK 이름을 동적으로 조회하여 삭제
SET @schema = DATABASE();

-- action_id_1 FK 삭제
SET @fk = (
    SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'quick_slot_presets'
      AND COLUMN_NAME = 'action_id_1'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @sql = IF(@fk IS NOT NULL,
    CONCAT('ALTER TABLE quick_slot_presets DROP FOREIGN KEY `', @fk, '`'),
    'SELECT ''action_id_1 FK not found, skipping''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- action_id_2 FK 삭제
SET @fk = (
    SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'quick_slot_presets'
      AND COLUMN_NAME = 'action_id_2'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @sql = IF(@fk IS NOT NULL,
    CONCAT('ALTER TABLE quick_slot_presets DROP FOREIGN KEY `', @fk, '`'),
    'SELECT ''action_id_2 FK not found, skipping''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- action_id_3 FK 삭제
SET @fk = (
    SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'quick_slot_presets'
      AND COLUMN_NAME = 'action_id_3'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @sql = IF(@fk IS NOT NULL,
    CONCAT('ALTER TABLE quick_slot_presets DROP FOREIGN KEY `', @fk, '`'),
    'SELECT ''action_id_3 FK not found, skipping''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- action_id_4 FK 삭제
SET @fk = (
    SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'quick_slot_presets'
      AND COLUMN_NAME = 'action_id_4'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @sql = IF(@fk IS NOT NULL,
    CONCAT('ALTER TABLE quick_slot_presets DROP FOREIGN KEY `', @fk, '`'),
    'SELECT ''action_id_4 FK not found, skipping''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- action_id_5 FK 삭제
SET @fk = (
    SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = @schema
      AND TABLE_NAME = 'quick_slot_presets'
      AND COLUMN_NAME = 'action_id_5'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @sql = IF(@fk IS NOT NULL,
    CONCAT('ALTER TABLE quick_slot_presets DROP FOREIGN KEY `', @fk, '`'),
    'SELECT ''action_id_5 FK not found, skipping''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 기존 컬럼 삭제 및 신규 컬럼 추가
-- ============================================================
ALTER TABLE quick_slot_presets
    DROP COLUMN IF EXISTS action_id_1,
    DROP COLUMN IF EXISTS action_id_2,
    DROP COLUMN IF EXISTS action_id_3,
    DROP COLUMN IF EXISTS action_id_4,
    DROP COLUMN IF EXISTS action_id_5,
    ADD COLUMN slot_id_1 BIGINT NULL,
    ADD COLUMN slot_id_2 BIGINT NULL,
    ADD COLUMN slot_id_3 BIGINT NULL,
    ADD COLUMN slot_id_4 BIGINT NULL,
    ADD COLUMN slot_id_5 BIGINT NULL,
    ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

-- ============================================================
-- 신규 FK 제약 추가 (quick_slots 참조)
-- ============================================================
ALTER TABLE quick_slot_presets
    ADD CONSTRAINT fk_preset_slot_1 FOREIGN KEY (slot_id_1) REFERENCES quick_slots (idx) ON DELETE SET NULL,
    ADD CONSTRAINT fk_preset_slot_2 FOREIGN KEY (slot_id_2) REFERENCES quick_slots (idx) ON DELETE SET NULL,
    ADD CONSTRAINT fk_preset_slot_3 FOREIGN KEY (slot_id_3) REFERENCES quick_slots (idx) ON DELETE SET NULL,
    ADD CONSTRAINT fk_preset_slot_4 FOREIGN KEY (slot_id_4) REFERENCES quick_slots (idx) ON DELETE SET NULL,
    ADD CONSTRAINT fk_preset_slot_5 FOREIGN KEY (slot_id_5) REFERENCES quick_slots (idx) ON DELETE SET NULL;

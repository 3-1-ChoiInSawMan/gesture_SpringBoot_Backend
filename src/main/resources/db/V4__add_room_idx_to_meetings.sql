ALTER TABLE meetings ADD COLUMN room_idx BIGINT;
UPDATE meetings m SET m.room_idx = (SELECT c.room_idx FROM calls c WHERE c.idx = m.call_idx);
ALTER TABLE meetings MODIFY COLUMN room_idx BIGINT NOT NULL;
ALTER TABLE meetings ADD CONSTRAINT fk_meetings_room FOREIGN KEY (room_idx) REFERENCES rooms(idx);

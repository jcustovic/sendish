ALTER TABLE photo_receiver ADD COLUMN pr_auto_received boolean;
UPDATE photo_receiver SET pr_auto_received = true;
ALTER TABLE photo_receiver ALTER COLUMN pr_auto_received SET NOT NULL;

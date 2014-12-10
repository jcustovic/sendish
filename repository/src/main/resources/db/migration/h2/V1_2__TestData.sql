INSERT INTO auth_user (au_id, au_username, au_password, au_modified_date, au_created_date, au_email_registration)
  VALUES (nextval('auth_user_seq'), 'test', '9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08', current_timestamp, current_timestamp, true);

INSERT INTO auth_user_details (aud_user_id, aud_last_interaction_time)
  VALUES(currval('auth_user_seq'), current_timestamp);

INSERT INTO auth_user_statistics (aus_user_id, aus_modified_date)
  VALUES(currval('auth_user_seq'), current_timestamp);

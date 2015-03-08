-- Country table
create sequence country_seq;

create table country (
  c_id int8 not null default nextval('country_seq'),
  c_name varchar(64) not null,
  c_iso varchar(2) not null unique,
  c_iso3 varchar(3) not null unique,
  c_currency varchar(32) not null,
  c_currency_code varchar(3) not null,

  primary key (c_id)
);

create index country_iso_idx on country (c_iso);

-- City table
create sequence city_seq;

create table city (
  ct_id int8 not null default nextval('city_seq'),
  ct_external_id int4 not null unique,
  ct_name varchar(200) not null,
  ct_latitude numeric(19, 10) not null,
  ct_longitude numeric(19, 10) not null,
  ct_location geometry not null,
  ct_country_id int8 not null,
  ct_population int4,
  ct_timezone varchar(32) not null,
  ct_deleted boolean not null default false,
  ct_created_date timestamp not null,

  primary key (ct_id),
  constraint city_country_id_fk foreign key (ct_country_id) references country
);

create index city_external_id_idx on city (ct_external_id);
create index city_country_id_idx on city (ct_country_id);

-- User table
create sequence auth_user_seq;

create table auth_user (
  au_id int8 not null default nextval('auth_user_seq'),
  au_username varchar(100) not null unique,
  au_email varchar(80) unique,
  au_password varchar(65) not null,
  au_nickname varchar(20),
  au_firstname varchar(128),
  au_lastname varchar(128),
  au_gender varchar(6),
  au_birthdate date,
  au_disabled boolean not null default false,
  au_deleted boolean not null default false,
  au_email_confirmed boolean not null default false,
  au_email_registration boolean not null default false,
  au_verification_code varchar(36),
  au_modified_date timestamp not null,
  au_created_date timestamp not null,

  primary key (au_id)
);

-- Users social connections
create table user_social_connection (
  usc_user_id int8 not null,
  usc_provider_id varchar(255) not null,
  usc_provider_user_id varchar(255) not null,
  usc_rank int4 not null,
  usc_display_name varchar(255),
  usc_profile_url varchar(512),
  usc_image_url varchar(512),
  usc_access_token varchar(255) not null,
  usc_secret varchar(255),
  usc_refresh_token varchar(255),
  usc_expire_time int8,
  usc_modified_date timestamp not null,

  primary key (usc_user_id, usc_provider_id, usc_provider_user_id),
  constraint user_social_connection_upr_uq unique (usc_user_id, usc_provider_id, usc_rank)
);

create index oauth2_search_idx on user_social_connection (usc_provider_id, usc_access_token, usc_refresh_token);
create index oauth1_search_idx on user_social_connection (usc_provider_id, usc_access_token, usc_secret);

-- UserDetails table
create table auth_user_details (
  aud_user_id int8 not null,
  aud_latitude numeric(19, 10),
  aud_longitude numeric(19, 10),
  aud_location geometry,
  aud_last_location_time timestamp,
  aud_current_city_id int8,
  aud_receive_limit_day int4 not null,
  aud_send_limit_day int4 not null,
  aud_receive_notifications boolean not null default false,
  aud_last_received_time timestamp,
  aud_last_sent_time timestamp,
  aud_receive_allowed_time timestamp,
  aud_send_allowed_time timestamp,
  aud_last_interaction_time timestamp,

  primary key (aud_user_id),
  constraint auth_user_status_user_id_fk foreign key (aud_user_id) references auth_user,
  constraint auth_user_status_city_id_fk foreign key (aud_current_city_id) references city
);

create index auth_user_details_user_idx on auth_user_details (aud_user_id);
create index auth_user_details_current_city_idx on auth_user_details (aud_current_city_id);

-- UserStatistics table
create table auth_user_statistics (
  aus_user_id int8 not null,
  aus_likes_count int4 not null default 0,
  aus_dislikes_count int4 not null default 0,
  aus_cities_count int4 not null default 0,
  aus_reports_count int4 not null default 0,
  aus_rank int4 null,
  aus_modified_date timestamp not null,

  primary key (aus_user_id),
  constraint auth_user_statistics_user_id_fk foreign key (aus_user_id) references auth_user
);

create index auth_user_statistics_user_idx on auth_user_statistics (aus_user_id);

-- Photo table
create sequence photo_seq;

create table photo (
  p_id int8 not null default nextval('photo_seq'),
  p_uuid varchar(36) not null unique,
  p_name varchar(128) not null,
  p_description varchar(200),
  p_user_id int8 not null,
  p_deleted boolean not null,
  p_sender_deleted boolean not null,
  p_storage_id varchar(200) not null unique,
  p_width int4 not null,
  p_height int4 not null,
  p_size_byte int8 not null,
  p_content_type varchar(128) not null,
  p_created_date timestamp not null,
  p_origin_latitude numeric(19, 10) not null,
  p_origin_longitude numeric(19, 10) not null,
  p_origin_location geometry not null,
  p_city_id int8 not null,

  primary key (p_id),
  constraint photo_user_id_fk foreign key (p_user_id) references auth_user,
  constraint photo_city_id_fk foreign key (p_city_id) references city
);

create index photo_user_id_idx on photo (p_user_id);
create index photo_city_id_idx on photo (p_city_id);

-- ResizedPhoto table
create sequence resized_photo_seq;

create table resized_photo (
  rp_id int8 not null default nextval('resized_photo_seq'),
  rp_photo_id int8 not null,
  rp_key varchar(32) not null,
  rp_storage_id varchar(200) not null unique,
  rp_width int4 not null,
  rp_height int4 not null,
  rp_size_byte int8 not null,
  rp_created_date timestamp not null,

  primary key (rp_id),
  constraint resized_photo_photo_id_fk foreign key (rp_photo_id) references photo,
  constraint resized_photo_photo_key_uq unique (rp_photo_id, rp_key)
);

create index resized_photo_photo_idx on resized_photo (rp_photo_id);
create index resized_photo_photo_key_idx on resized_photo (rp_photo_id, rp_key);

-- PhotoComment table
create sequence photo_comment_seq;

create table photo_comment (
  pc_id int8 not null default nextval('photo_comment_seq'),
  pc_photo_id int8 not null,
  pc_user_id int8 not null,
  pc_comment varchar(200),
  pc_likes_count int4 not null default 0,
  pc_dislikes_count int4 not null default 0,
  pc_reports_count int4 not null default 0,
  pc_deleted boolean not null,
  pc_created_date timestamp not null,

  primary key (pc_id),
  constraint photo_comment_user_id_fk foreign key (pc_user_id) references auth_user,
  constraint photo_comment_photo_id_fk foreign key (pc_photo_id) references photo
);

create index photo_comment_user_idx on photo_comment (pc_user_id);
create index photo_comment_photo_idx on photo_comment (pc_photo_id);

-- PhotoCommentVote table
create table photo_comment_vote (
  pcv_pc_id int8 not null,
  pcv_user_id int8 not null,
  pcv_like boolean not null,
  pcv_created_date timestamp not null,

  primary key (pcv_pc_id, pcv_user_id),
  constraint photo_comment_vote_user_id_fk foreign key (pcv_user_id) references auth_user,
  constraint photo_comment_vote_photo_comment_id_fk foreign key (pcv_pc_id) references photo_comment
);

create index photo_comment_vote_user_idx on photo_comment_vote (pcv_user_id);
create index photo_comment_vote_photo_comment_idx on photo_comment_vote (pcv_pc_id);

-- PhotoReceiver table
create sequence photo_receiver_seq;

create table photo_receiver (
  pr_id int8 not null default nextval('photo_receiver_seq'),
  pr_photo_id int8 not null,
  pr_user_id int8 not null,
  pr_like boolean null,
  pr_deleted boolean not null,
  pr_report boolean null,
  pr_report_type varchar(32),
  pr_report_text varchar(128),
  pr_opened_latitude numeric(19, 10) null,
  pr_opened_longitude numeric(19, 10) null,
  pr_opened_location geometry null,
  pr_city_id int8 null,
  pr_created_date timestamp not null,
  pr_opened_date timestamp null,

  primary key (pr_id),
  constraint photo_receiver_user_photo_id_uq unique (pr_photo_id, pr_user_id),
  constraint photo_receiver_user_id_fk foreign key (pr_user_id) references auth_user,
  constraint photo_receiver_photo_id_fk foreign key (pr_photo_id) references photo,
  constraint photo_receiver_city_id_fk foreign key (pr_city_id) references city
);

create index photo_receiver_user_idx on photo_receiver (pr_user_id);
create index photo_receiver_photo_idx on photo_receiver (pr_photo_id);
create index photo_receiver_city_idx on photo_receiver (pr_city_id);

-- PhotoSendingDetails table
create table photo_sending_details (
  psd_photo_id int8 not null,
  psd_last_photo_rec_id int8 null,
  psd_photo_status varchar(9) not null,
  psd_send_status varchar(7) null,
  psd_version int4,

  primary key (psd_photo_id),
  constraint photo_sending_details_photo_id_fk foreign key (psd_photo_id) references photo,
  constraint photo_sending_details_photo_receiver_id_fk foreign key (psd_last_photo_rec_id) references photo_receiver
);

create index photo_sending_details_photo_idx on photo_sending_details (psd_photo_id);

-- PhotoStatistics table
create table photo_statistics (
  pst_photo_id int8 not null,
  pst_likes_count int4 not null default 0,
  pst_dislikes_count int4 not null default 0,
  pst_reports_count int4 not null default 0,
  pst_cities_count int4 not null default 0,
  pst_countries_count int4 not null default 0,
  pst_comments_count int4 not null default 0,
  pst_users_count int4 not null default 0,
  pst_modified_date timestamp not null,

  primary key (pst_photo_id),
  constraint photo_statistics_photo_id_fk foreign key (pst_photo_id) references photo
);

create index photo_statistics_photo_idx on photo_statistics (pst_photo_id);

-- Push notifications related tables
create sequence notification_message_seq;

create table notification_message (
  nm_id int8 not null default nextval('notification_message_seq'),
	nm_notification_type varchar(15) not null,
	nm_ref_id int8,
	nm_status varchar(10) not null,
	nm_created_date timestamp not null,
	nm_done_sending_date timestamp,
	nm_finished_date timestamp,
	nm_gcm_count int4 not null,
	nm_gcm_success int4 not null,
	nm_apns_count int4 not null,
	nm_apns_success int4 not null,

	primary key (nm_id)
);

create sequence notification_partial_result_seq;

create table notification_partial_result (
	npr_id int8 not null default nextval('notification_partial_result_seq'),
	npr_platform_type varchar(4) not null,
	npr_send_date timestamp not null,
	npr_response_date timestamp not null,
	npr_notification_msg_id int8 not null,
	npr_total_count int4 not null,
	npr_failure_count int4 not null,

	primary key (npr_id),
	constraint notification_partial_result_not_msg_id_fk foreign key (npr_notification_msg_id) references notification_message
);

create index notification_partial_result_msg_id_idx on notification_partial_result (npr_notification_msg_id);

create sequence push_notification_token_seq;

create table push_notification_token (
  pt_id int8 not null default nextval('push_notification_token_seq'),
  pt_token varchar(200) not null,
  pt_platform_type varchar(4) not null,
  pt_modified_date timestamp not null,
  pt_user_id int8 not null,
  pt_dev_token boolean not null default false,

  primary key (pt_id),
  constraint push_notification_token_user_id_fk foreign key (pt_user_id) references auth_user,
  constraint push_notification_token_uq unique (pt_token, pt_platform_type)
);

create index push_notification_token_user_id_idx on push_notification_token (pt_user_id);

-- Image table
create sequence image_seq;

create table image (
  i_id int8 not null default nextval('image_seq'),
  i_uuid varchar(36) not null unique,
  i_name varchar(128) not null,
  i_storage_id varchar(200) not null unique,
  i_width int4 not null,
  i_height int4 not null,
  i_size_byte int8 not null,
  i_content_type varchar(128) not null,
  i_created_date timestamp not null,

  primary key (i_id)
);

-- ResizedImage table
create sequence resized_image_seq;

create table resized_image (
  ri_id int8 not null default nextval('resized_image_seq'),
  ri_image_id int8 not null,
  ri_key varchar(32) not null,
  ri_storage_id varchar(200) not null unique,
  ri_width int4 not null,
  ri_height int4 not null,
  ri_size_byte int8 not null,
  ri_created_date timestamp not null,

  primary key (ri_id),
  constraint resized_image_image_id_fk foreign key (ri_image_id) references image,
  constraint resized_image_image_key_uq unique (ri_image_id, ri_key)
);

create index resized_image_image_idx on resized_image (ri_image_id);
create index resized_image_image_key_idx on resized_image (ri_image_id, ri_key);

-- InboxMessage table
create sequence inbox_message_seq;

create table inbox_message (
  im_id int8 not null default nextval('inbox_message_seq'),
  im_short_title varchar(64) not null,
  im_title varchar(256) not null,
  im_message text not null,
  im_url varchar(256),
  im_image_id int8 not null,
  im_created_date timestamp not null,

  primary key (im_id),
  constraint inbox_message_image_id_fk foreign key (im_image_id) references image
);

-- UserInbox table
create sequence user_inbox_item_seq;

create table user_inbox_item (
  uii_id int8 not null default nextval('user_inbox_item_seq'),
  uii_user_id int8 not null,
  uii_inbox_message_id int8 not null unique,
  uii_first_opened_date timestamp,
  uii_deleted boolean not null default false,
  uii_read boolean not null default false,
  uii_created_date timestamp not null,

  primary key (uii_id),
  constraint user_inbox_item_user_id_fk foreign key (uii_user_id) references auth_user,
  constraint user_inbox_item_inbox_message_id_fk foreign key (uii_inbox_message_id) references inbox_message
);

create index user_inbox_item_user_idx on user_inbox_item (uii_user_id);

-- HotPhoto table
create table hot_photo (
  hp_photo_id int8 not null,
  hp_selected_time timestamp not null,
  hp_removed_time timestamp,

  primary key (hp_photo_id),
  constraint hot_photo_photo_id_fk foreign key (hp_photo_id) references photo
);

-- UserActivity table
create sequence user_activity_seq;

create table user_activity (
  ua_id int8 not null default nextval('user_activity_seq'),
  ua_user_id int8 not null,
  ua_from_user_id int8,
  ua_text varchar(200) not null,
  ua_image_uuid varchar(36),
  ua_reference_type varchar(32) not null,
  ua_reference_id varchar(32),
  ua_created_date timestamp,

  primary key (ua_id),
  constraint user_activity_user_id_fk foreign key (ua_user_id) references auth_user,
  constraint user_activity_from_user_id_fk foreign key (ua_from_user_id) references auth_user
);

create index user_activity_user_idx on user_activity (ua_user_id);

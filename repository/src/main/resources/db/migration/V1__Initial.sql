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
  au_nickname varchar(8),
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

create INDEX oauth2_search_idx on user_social_connection (usc_provider_id, usc_access_token, usc_refresh_token);
create INDEX oauth1_search_idx on user_social_connection (usc_provider_id, usc_access_token, usc_secret);

-- UserDetails table
create table auth_user_details (
  aud_user_id int8 not null,
  aud_latitude numeric(19, 10),
  aud_longitude numeric(19, 10),
  aud_location geometry,
  aud_last_location_time timestamp,
  aud_current_city_id int8,
  aud_receive_limit_day int4,
  aud_send_limit_day int4,
  aud_today_limit_count int4,
  aud_limit_day date,
  aud_last_interaction_time timestamp not null,

  primary key (aud_user_id),
  constraint auth_user_status_user_id_fk foreign key (aud_user_id) references auth_user,
  constraint auth_user_status_city_id_fk foreign key (aud_current_city_id) references city
);

-- UserStatistics table
create table auth_user_statistics (
  aus_user_id int8 not null,
  aus_likes_count int4 not null default 0,
  aus_dislikes_count int4 not null default 0,
  aus_reports_count int4 not null default 0,
  aus_rank int4 null,
  aus_modified_date timestamp not null,

  primary key (aus_user_id),
  constraint auth_user_statistics_user_id_fk foreign key (aus_user_id) references auth_user
);

-- Photo table
create sequence photo_seq;

create table photo (
  p_id int8 not null default nextval('photo_seq'),
  p_uuid varchar(36) not null unique,
  p_name varchar(128) not null,
  p_description varchar(200),
  p_user_id int8 not null,
  p_resend boolean not null,
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

-- PhotoComment table
create sequence photo_comment_seq;

create table photo_comment (
  pc_id int8 not null default nextval('photo_comment_seq'),
  pc_photo_id int8 not null,
  pc_user_id int8 not null,
  pc_comment varchar(128),
  pc_likes_count int4 not null default 0,
  pc_dislikes_count int4 not null default 0,
  pc_reports_count int4 not null default 0,
  pc_deleted boolean not null,
  pc_created_date timestamp not null,

  primary key (pc_id),
  constraint photo_comment_user_id_fk foreign key (pc_user_id) references auth_user,
  constraint photo_comment_photo_id_fk foreign key (pc_photo_id) references photo
);

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
  constraint photo_receiver_user_id_fk foreign key (pr_user_id) references auth_user,
  constraint photo_receiver_photo_id_fk foreign key (pr_photo_id) references photo,
  constraint photo_receiver_city_id_fk foreign key (pr_city_id) references city
);

-- PhotoStatus table
create table photo_status (
  ps_photo_id int8 not null,
  ps_last_photo_rec_id int8 null,
  ps_resend_stopped boolean not null default 0,

  primary key (ps_photo_id),
  constraint photo_status_photo_id_fk foreign key (ps_photo_id) references photo,
  constraint photo_status_ps_photo_receiver_id_fk foreign key (ps_last_photo_rec_id) references photo_receiver,
);

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


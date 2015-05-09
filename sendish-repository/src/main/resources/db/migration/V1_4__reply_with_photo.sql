-- PhotoReply table
create sequence photo_reply_seq;

create table photo_reply (
  prp_id int8 not null default nextval('photo_reply_seq'),
  prp_uuid varchar(36) not null unique,
  prp_name varchar(128) not null,
  prp_description varchar(200),
  prp_photo_id int8 not null,
  prp_user_id int8 not null,
  prp_deleted boolean not null,
  prp_storage_id varchar(200) not null unique,
  prp_width int4 not null,
  prp_height int4 not null,
  prp_size_byte int8 not null,
  prp_content_type varchar(128) not null,
  prp_created_date timestamp not null,
  prp_report_type varchar(32),
  prp_report_text varchar(128),
  prp_reported_by int8,

  primary key (prp_id),
  constraint photo_reply_photo_id_fk foreign key (prp_photo_id) references photo,
  constraint photo_reply_user_id_fk foreign key (prp_user_id) references auth_user,
  constraint photo_reply_reported_by_fk foreign key (prp_reported_by) references auth_user,
  constraint photo_reply_user_photo_uq unique (prp_user_id, prp_photo_id)
);

create index photo_reply_photo_id_idx on photo_reply (prp_photo_id);
create index photo_reply_user_id_idx on photo_reply (prp_user_id);
create index photo_reply_reported_by_idx on photo_reply (prp_reported_by);


-- ChatThread table
create sequence chat_thread_seq;

create table chat_thread (
  cth_id int8 not null default nextval('chat_thread_seq'),
  cth_photo_reply_id int8 not null,
  cth_last_activity_time timestamp not null,
  cth_created_date timestamp not null,

  primary key (cth_id),
  constraint chat_thread_photo_reply_id_fk foreign key (cth_photo_reply_id) references photo_reply
);

create index chat_thread_photo_reply_id_idx on chat_thread (cth_photo_reply_id);


-- ChatThreadUser table
create table chat_thread_user (
  ctu_thread_id int8 not null,
  ctu_user_id int8 not null,
  ctu_thread_name varchar(64),

  primary key (ctu_thread_id, ctu_user_id),
  constraint chat_thread_user_thread_id_fk foreign key (ctu_thread_id) references chat_thread,
  constraint chat_thread_user_user_id_fk foreign key (ctu_user_id) references auth_user
);

create index chat_thread_user_thread_idx on chat_thread_user (ctu_thread_id);
create index chat_thread_user_user_idx on chat_thread_user (ctu_user_id);


-- ChatMessage table
create sequence chat_message_seq;

create table chat_message (
  cm_id int8 not null default nextval('chat_message_seq'),
  cm_thread_id int8 not null,
  cm_text varchar(1024),
  cm_deleted boolean not null,
  cm_created_date timestamp not null,

  primary key (cm_id),
  constraint chat_message_thread_id_fk foreign key (cm_thread_id) references chat_thread
);

create index chat_message_thread_id_idx on chat_message (cm_thread_id);

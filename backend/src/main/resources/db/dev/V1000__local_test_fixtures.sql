INSERT INTO app_user(auth_provider,provider_subject,created_at,updated_at) VALUES ('GOOGLE','dev-test-sub',now(),now()),('GOOGLE','ownership-test-sub',now(),now());
INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(1,'テストPL',now(),now());
INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(1,'参加者 2',now(),now());
INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(1,'同名の人物',now(),now());
INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(1,'同名の人物',now(),now());
INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(1,'参加者 5',now(),now());
INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(1,'参加者 6',now(),now());
INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(1,'参加者 7',now(),now());
INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(1,'参加者 8',now(),now());
INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(1,'参加者 9',now(),now());
INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(1,'参加者 10',now(),now());
INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(1,'参加者 11',now(),now());
INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(1,'参加者 12',now(),now());
INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(2,'別ユーザー',now(),now());
UPDATE app_user SET self_person_id=1 WHERE id=1;UPDATE app_user SET self_person_id=13 WHERE id=2;
INSERT INTO scenario(user_id,name,author_name,game_system,source_url,external_image_url,created_at,updated_at) VALUES(1,'夏の終わりに','テスト用作者','クトゥルフ神話TRPG 7版','https://example.invalid/scenario/1',NULL,now(),now());
INSERT INTO scenario(user_id,name,author_name,game_system,source_url,external_image_url,created_at,updated_at) VALUES(1,'静かな回廊','テスト用作者','クトゥルフ神話TRPG 6版','https://example.invalid/scenario/2',NULL,now(),now());
INSERT INTO scenario(user_id,name,author_name,game_system,source_url,external_image_url,created_at,updated_at) VALUES(1,'テスト作品 3','テスト用作者','エモクロアTRPG','https://example.invalid/scenario/3',NULL,now(),now());
INSERT INTO scenario(user_id,name,author_name,game_system,source_url,external_image_url,created_at,updated_at) VALUES(1,'テスト作品 4','テスト用作者','クトゥルフ神話TRPG 7版','https://example.invalid/scenario/4',NULL,now(),now());
INSERT INTO scenario(user_id,name,author_name,game_system,source_url,external_image_url,created_at,updated_at) VALUES(1,'テスト作品 5','テスト用作者','クトゥルフ神話TRPG 6版','https://example.invalid/scenario/5',NULL,now(),now());
INSERT INTO scenario(user_id,name,author_name,game_system,source_url,external_image_url,created_at,updated_at) VALUES(1,'テスト作品 6','テスト用作者','エモクロアTRPG','https://example.invalid/scenario/6',NULL,now(),now());
INSERT INTO scenario(user_id,name,author_name,game_system,source_url,external_image_url,created_at,updated_at) VALUES(1,'テスト作品 7','テスト用作者','クトゥルフ神話TRPG 7版','https://example.invalid/scenario/7',NULL,now(),now());
INSERT INTO scenario(user_id,name,author_name,game_system,source_url,external_image_url,created_at,updated_at) VALUES(1,'テスト作品 8','テスト用作者','クトゥルフ神話TRPG 6版','https://example.invalid/scenario/8',NULL,now(),now());
INSERT INTO scenario(user_id,name,author_name,game_system,source_url,external_image_url,created_at,updated_at) VALUES(1,'テスト作品 9','テスト用作者','エモクロアTRPG','https://example.invalid/scenario/9',NULL,now(),now());
INSERT INTO scenario(user_id,name,author_name,game_system,source_url,external_image_url,created_at,updated_at) VALUES(1,'テスト作品 10','テスト用作者','クトゥルフ神話TRPG 7版','https://example.invalid/scenario/10','https://example.invalid/external-fixture.png',now(),now());
INSERT INTO scenario(user_id,name,created_at,updated_at) VALUES(2,'別ユーザー作品',now(),now());
INSERT INTO scenario_favorite(user_id,scenario_id,created_at) VALUES(1,1,now()),(1,3,now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,1,'五色 探',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(1,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,2,'火守 蓮',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(2,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,3,'同名のPC',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(3,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,4,'同名のPC',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(4,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,5,'テストPC 5',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(5,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,6,'テストPC 6',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(6,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,7,'テストPC 7',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(7,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,8,'テストPC 8',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(8,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,9,'テストPC 9',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(9,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,10,'テストPC 10',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(10,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,11,'テストPC 11',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(11,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,12,'テストPC 12',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(12,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,12,'テストPC 13',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(13,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,12,'テストPC 14',now(),now());
INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(14,'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());
INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(2,13,'別ユーザーPC',now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,1,NULL,NULL,now()+interval '1 seconds',now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(1,date '2026-09-01'+1,now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,1,NULL,'https://example.invalid/recording/2',now()+interval '2 seconds',now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(2,date '2026-09-01'+2,now(),now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(2,date '2026-09-01'+3,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(2,1,'KP',1,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(2,2,'KP',2,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(2,1,NULL,'PL',1,'HO1',NULL,'HO',now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,1,'記録 3',NULL,now()+interval '3 seconds',now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(3,date '2026-09-01'+3,now(),now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(3,date '2026-09-01'+4,now(),now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(3,date '2026-09-01'+5,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(3,1,'KP',1,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(3,1,1,'PL',1,'HO1',NULL,'HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(5,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','SURVIVED',NULL,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(3,2,2,'PL',2,'HO2','帰ろう。','HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(6,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','LOST','記憶が曖昧になった。',now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,1,NULL,'https://example.invalid/recording/4',now()+interval '4 seconds',now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(4,1,'KP',1,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(4,2,'KP',2,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(4,1,1,'PL',1,'HO1',NULL,'HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(9,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','SURVIVED',NULL,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(4,2,2,'PL',2,'HO2','帰ろう。','HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(10,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','LOST','記憶が曖昧になった。',now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(4,3,NULL,'PL',3,NULL,'帰ろう。','QUOTE',now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,1,NULL,NULL,now()+interval '5 seconds',now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(5,date '2026-09-01'+5,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(5,1,'KP',1,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(5,1,1,'PL',1,'HO1',NULL,'HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(13,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','SURVIVED',NULL,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(5,2,2,'PL',2,'HO2','帰ろう。','HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(14,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','LOST','記憶が曖昧になった。',now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(5,3,3,'PL',3,NULL,'帰ろう。','QUOTE',now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(5,4,4,'PL',4,'HO4',NULL,'HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(16,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','LOST','記憶が曖昧になった。',now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,1,'記録 6','https://example.invalid/recording/6',now()+interval '6 seconds',now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(6,date '2026-09-01'+6,now(),now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(6,date '2026-09-01'+7,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(6,1,'KP',1,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(6,2,'KP',2,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(6,1,1,'PL',1,'HO1',NULL,'HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(19,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','SURVIVED',NULL,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(6,2,2,'PL',2,'HO2','帰ろう。','HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(20,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','LOST','記憶が曖昧になった。',now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(6,3,3,'PL',3,NULL,'帰ろう。','QUOTE',now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(6,4,4,'PL',4,'HO4',NULL,'HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(22,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','LOST','記憶が曖昧になった。',now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(6,5,NULL,'PL',5,'HO5','帰ろう。','HO',now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,1,NULL,NULL,now()+interval '7 seconds',now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(7,date '2026-09-01'+7,now(),now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(7,date '2026-09-01'+8,now(),now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(7,date '2026-09-01'+9,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(7,1,'KP',1,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(7,1,1,'PL',1,'HO1',NULL,'HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(25,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','SURVIVED',NULL,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(7,2,2,'PL',2,'HO2','帰ろう。','HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(26,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','LOST','記憶が曖昧になった。',now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(7,3,3,'PL',3,NULL,'帰ろう。','QUOTE',now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(7,4,4,'PL',4,'HO4',NULL,'HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(28,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','LOST','記憶が曖昧になった。',now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(7,5,5,'PL',5,'HO5','帰ろう。','HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(29,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','SURVIVED',NULL,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(7,6,6,'PL',6,NULL,'帰ろう。','QUOTE',now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,1,NULL,'https://example.invalid/recording/8',now()+interval '8 seconds',now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(8,1,'KP',1,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(8,2,'KP',2,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(8,1,1,'PL',1,'HO1',NULL,'HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(33,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','SURVIVED',NULL,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(8,2,2,'PL',2,'HO2','帰ろう。','HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(34,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','LOST','記憶が曖昧になった。',now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(8,3,3,'PL',3,NULL,'帰ろう。','QUOTE',now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(8,4,4,'PL',4,'HO4',NULL,'HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(36,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','LOST','記憶が曖昧になった。',now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(8,5,5,'PL',5,'HO5','帰ろう。','HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(37,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','SURVIVED',NULL,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(8,6,6,'PL',6,NULL,'帰ろう。','QUOTE',now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(8,7,NULL,'PL',7,'HO7',NULL,'HO',now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,2,'記録 9',NULL,now()+interval '9 seconds',now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(9,date '2026-09-01'+9,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(9,1,'KP',1,now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,2,NULL,'https://example.invalid/recording/10',now()+interval '10 seconds',now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(10,date '2026-09-01'+10,now(),now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(10,date '2026-09-01'+11,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(10,1,'KP',1,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(10,2,'KP',2,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(10,1,NULL,'PL',1,'HO1',NULL,'HO',now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,4,NULL,NULL,now()+interval '11 seconds',now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(11,date '2026-09-01'+11,now(),now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(11,date '2026-09-01'+12,now(),now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(11,date '2026-09-01'+13,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(11,1,'KP',1,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(11,1,1,'PL',1,'HO1',NULL,'HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(45,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','SURVIVED',NULL,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(11,2,2,'PL',2,'HO2','帰ろう。','HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(46,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','LOST','記憶が曖昧になった。',now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,5,'記録 12','https://example.invalid/recording/12',now()+interval '12 seconds',now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(12,1,'KP',1,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(12,2,'KP',2,now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,6,NULL,NULL,now()+interval '13 seconds',now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(13,date '2026-09-01'+13,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(13,1,'KP',1,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(13,1,1,'PL',1,'HO1',NULL,'HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(50,'emoklore_v1','{"hp":10,"mp":12}','目星 +3
記録の続き','SURVIVED',NULL,now(),now());
INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,7,NULL,'https://example.invalid/recording/14',now()+interval '14 seconds',now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(14,date '2026-09-01'+14,now(),now());
INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(14,date '2026-09-01'+15,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(14,1,'KP',1,now(),now());
INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(14,2,'KP',2,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(14,1,1,'PL',1,'HO1',NULL,'HO',now(),now());
INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(53,'coc_7e_v1','{"san":51,"hp":10,"mp":12}','目星 +3
記録の続き','SURVIVED',NULL,now(),now());
INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(14,2,NULL,'PL',2,'HO2','帰ろう。','HO',now(),now());

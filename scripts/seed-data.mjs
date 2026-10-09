import fs from 'node:fs';
const quote=s=>s==null?'NULL':"'"+s.replaceAll("'","''")+"'";
let sql=`INSERT INTO app_user(auth_provider,provider_subject,created_at,updated_at) VALUES ('GOOGLE','dev-test-sub',now(),now()),('GOOGLE','ownership-test-sub',now(),now());\n`;
for(let i=1;i<=12;i++)sql+=`INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(1,${quote(i===1?'テストPL':i===3?'同名の人物':i===4?'同名の人物':'参加者 '+i)},now(),now());\n`;
sql+=`INSERT INTO person(user_id,display_name,created_at,updated_at) VALUES(2,'別ユーザー',now(),now());\nUPDATE app_user SET self_person_id=1 WHERE id=1;UPDATE app_user SET self_person_id=13 WHERE id=2;\n`;
for(let i=1;i<=10;i++)sql+=`INSERT INTO scenario(user_id,name,author_name,game_system,source_url,external_image_url,created_at,updated_at) VALUES(1,${quote(i===1?'夏の終わりに':i===2?'静かな回廊':'テスト作品 '+i)},'テスト用作者',${quote(i%3===0?'エモクロアTRPG':i%3===1?'クトゥルフ神話TRPG 7版':'クトゥルフ神話TRPG 6版')},'https://example.invalid/scenario/${i}',${i===10?"'https://example.invalid/external-fixture.png'":'NULL'},now(),now());\n`;
sql+=`INSERT INTO scenario(user_id,name,created_at,updated_at) VALUES(2,'別ユーザー作品',now(),now());\nINSERT INTO scenario_favorite(user_id,scenario_id,created_at) VALUES(1,1,now()),(1,3,now());\n`;
for(let i=1;i<=14;i++){sql+=`INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(1,${i===1?1:Math.min(12,i)},${quote(i===1?'五色 探':i===2?'火守 蓮':i===3||i===4?'同名のPC':'テストPC '+i)},now(),now());\n`;sql+=`INSERT INTO pc_system_setting(pc_id,game_system_name,canonical_system_key,profile_key,profile_values,display_order,created_at,updated_at) VALUES(${i},'クトゥルフ神話TRPG 7版','coc_7e','coc_7e_v1','{}',1,now(),now());\n`;}
sql+=`INSERT INTO pc(user_id,person_id,name,created_at,updated_at) VALUES(2,13,'別ユーザーPC',now(),now());\n`;
let part=0;
for(let t=1;t<=14;t++){
 const scenario=t<=8?1:t<=10?2:t-7;const count=t<=8?t-1:t%3;
 sql+=`INSERT INTO trpg_table(user_id,scenario_id,table_name,recording_url,created_at,updated_at) VALUES(1,${scenario},${t%3===0?quote('記録 '+t):'NULL'},${t%2===0?quote('https://example.invalid/recording/'+t):'NULL'},now()+interval '${t} seconds',now());\n`;
 for(let d=0;d<t%4;d++)sql+=`INSERT INTO table_date(table_id,played_on,created_at,updated_at) VALUES(${t},date '2026-09-01'+${t+d},now(),now());\n`;
 if(t!==1){part++;sql+=`INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(${t},1,'KP',1,now(),now());\n`;if(t%2===0){part++;sql+=`INSERT INTO participation(table_id,person_id,role,display_order,created_at,updated_at) VALUES(${t},2,'KP',2,now(),now());\n`;}}
 for(let p=1;p<=count;p++){part++;const pc=p===count&&t%2===0?null:p;const ho=p%3===0?null:'HO'+p;const copy=p%3===1?null:'帰ろう。';sql+=`INSERT INTO participation(table_id,person_id,pc_id,role,display_order,ho,display_quote,spotlight_type,created_at,updated_at) VALUES(${t},${p},${pc??'NULL'},'PL',${p},${quote(ho)},${quote(copy)},${quote(ho?'HO':copy?'QUOTE':null)},now(),now());\n`;if(pc&&p%3!==0)sql+=`INSERT INTO end_pc_state(participation_id,profile_key,status_values,growth,outcome,aftereffects,created_at,updated_at) VALUES(${part},${quote(scenario%3===0?'emoklore_v1':scenario%3===1?'coc_7e_v1':'coc_6e_v1')},${quote(scenario%3===0?'{"hp":10,"mp":12}':'{"san":51,"hp":10,"mp":12}')},'目星 +3\n記録の続き',${quote(p%2===0?'LOST':'SURVIVED')},${p%2===0?"'記憶が曖昧になった。'":'NULL'},now(),now());\n`;}
}
fs.mkdirSync('backend/src/main/resources/db/dev',{recursive:true});fs.writeFileSync('backend/src/main/resources/db/dev/V1000__local_test_fixtures.sql',sql);
console.log('Created local QA seed SQL; no production users.');

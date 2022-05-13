

-- INSERT INTO USER (ID, LOGIN_ID, PASSWORD, NICKNAME, ACTIVATED, EMAIL, NAME, COLLEGE_CODE, MAJOR_CODE, STUDENT_NUM, EMAIL_AUTHENTICATION, DELETED) VALUES (1, 'admin', '$2a$08$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi', 'admin', 1, '11111@naver.com', '어드민', '001', 'm001', 32210311, FALSE, FALSE);

-- 서버 실행과 동시에 실행되는 상황.
INSERT INTO user (ID, LOGIN_ID, PASSWORD, NICKNAME, ACTIVATED, EMAIL, name, COLLEGE_CODE, MAJOR_CODE, STUDENT_NUM, EMAIL_AUTHENTICATION, DELETED) VALUES (1, 'admin', '$2a$10$JwoSm1YVIbuY8u1WIS.XMu/G4npvQv60jAzIlu4WcnXIPuEBJz5.C', 'admin', 1, '11111@naver.com', 'admin', 'college0001', 'major0000', 32210311, FALSE, FALSE);
INSERT INTO user (ID, LOGIN_ID, PASSWORD, NICKNAME, ACTIVATED, EMAIL, name, COLLEGE_CODE, MAJOR_CODE, STUDENT_NUM, EMAIL_AUTHENTICATION, DELETED) VALUES (2, 'user1', '$2a$10$nicQMhS9JcPhrd3E/gc8gOHran0UsHvcIjfUUu9qGA.xD4rbaXbG2', 'nickuser1', 1, '22222@naver.com', 'seilpark', 'college0001', 'major0001', 32210312, FALSE, FALSE);
INSERT INTO user (ID, LOGIN_ID, PASSWORD, NICKNAME, ACTIVATED, EMAIL, name, COLLEGE_CODE, MAJOR_CODE, STUDENT_NUM, EMAIL_AUTHENTICATION, DELETED) VALUES (3, 'user2', '$2a$10$nicQMhS9JcPhrd3E/gc8gOHran0UsHvcIjfUUu9qGA.xD4rbaXbG2', 'nickuser2', 1, '33333@naver.com', 'syun', 'college0002', 'major0002', 32210313, FALSE, FALSE);

INSERT INTO authority (AUTHORITY_NAME) values ('ROLE_USER');
INSERT INTO authority (AUTHORITY_NAME) values ('ROLE_ADMIN');

INSERT INTO user_authority (USER_ID, AUTHORITY_NAME) values (1, 'ROLE_USER');
INSERT INTO user_authority (USER_ID, AUTHORITY_NAME) values (1, 'ROLE_ADMIN');
INSERT INTO user_authority (USER_ID, AUTHORITY_NAME) values (2, 'ROLE_USER');
INSERT INTO user_authority (USER_ID, AUTHORITY_NAME) values (3, 'ROLE_USER');

-- friend 신청(user1이 user2에게 신청)
INSERT INTO friend (ID, STATUS, RECIPIENT_ID, REQUESTOR_ID) values (1, 1, 3, 2);

-- user1,2 사진 추가
INSERT INTO user_picture (ID, STORED_FILE_PATH, USER_ID) values (1, 'https://d1cy8kjxuu1lsp.cloudfront.net/2022/post/20220504/e43e5a59-3b08-4fac-9c36-27468edc12da83851993430251user1.jpg', 2);
INSERT INTO user_picture (ID, STORED_FILE_PATH, USER_ID) values (2, 'https://d1cy8kjxuu1lsp.cloudfront.net/2022/post/20220504/a43e5a59-3b08-4fac-9c36-27468edc11da83851993430251user2.jpg', 3);
-- CREATEAT(필요하면): 2022-05-03 20:40:15.943059

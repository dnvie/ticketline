DELETE
FROM ticket;
DELETE
FROM performance_performers;
DELETE
FROM performance;
DELETE
FROM event;
DELETE
FROM location;
DELETE
FROM seat;
DELETE
FROM sector;
DELETE
FROM seat_map;
DELETE FROM seen_news;
DELETE FROM application_user;
DELETE FROM message;


INSERT INTO message (id, published_at, summary, text, title, image)
VALUES (-1L, '2023-06-25 12:00:00', 'testSummary1', 'testText1', 'testTitle1', '1'),
       (-2L, '2023-06-24 12:00:00', 'testSummary2', 'testText2', 'testTitle2', '1'),
       (-3L, '2023-06-23 12:00:00', 'testSummary3', 'testText3', 'testTitle3', '1'),
       (-4L, '2023-06-22 12:00:00', 'testSummary4', 'testText4', 'testTitle4', '1'),
       (-5L, '2023-06-21 12:00:00', 'testSummary5', 'testText5', 'testTitle5', '1'),
       (-6L, '2023-06-20 12:00:00', 'testSummary6', 'testText6', 'testTitle6', '1'),
       (-7L, '2023-06-19 12:00:00', 'testSummary7', 'testText7', 'testTitle7', '1'),
       (-8L, '2023-06-18 12:00:00', 'testSummary8', 'testText8', 'testTitle8', '1'),
       (-9L, '2023-06-17 12:00:00', 'testSummary9', 'testText9', 'testTitle9', '1'),
       (-10L, '2023-06-16 12:00:00', 'testSummary10', 'testText10', 'testTitle10', '1')
;

INSERT INTO application_user (id, email, password, admin, first_name, last_name, phone_number, enabled, counter)
VALUES ('90857b96-a69b-466b-90de-08c0ea4e66f4', 'test@email.com', 'test', false, 'test', 'user', '+43 198291', true, 0),
       ('ac55a452-f33d-42fe-9e85-185fc1f273ba', 'admin@email.com', 'admin', true, 'admin', 'user', '+49 123456', true,
        1);

INSERT INTO seen_news (id, user_id, news_id)
VALUES (-1L, 'ac55a452-f33d-42fe-9e85-185fc1f273ba', -1L),
       (-2L, 'ac55a452-f33d-42fe-9e85-185fc1f273ba', -2L),
       (-3L, 'ac55a452-f33d-42fe-9e85-185fc1f273ba', -3L),
       (-4L, 'ac55a452-f33d-42fe-9e85-185fc1f273ba', -4L),
       (-5L, 'ac55a452-f33d-42fe-9e85-185fc1f273ba', -5L),
       (-6L, 'ac55a452-f33d-42fe-9e85-185fc1f273ba', -6L)
;
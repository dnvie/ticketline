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


INSERT INTO event (id, title, type, begin_date, end_date, image)
VALUES (-1L, 'testConcert', '0', '2000-09-28', '2023-09-28', '1'),
       (-2L, 'testFestival', '1', '2023-06-25', '2023-06-29', '2'),
       (-3L, 'testTheater', '2', '2023-06-25', '2023-06-29', '3'),
       (-4L, 'testOpera', '3', '2023-06-25', '2023-06-29', '4'),
       (-5L, 'testMusical', '4', '2023-06-25', '2023-06-29', '5'),
       (-6L, 'testMovie', '5', '2023-06-25', '2023-06-29', '6'),
       (-7L, 'testOther', '6', '2023-06-25', '2023-06-29', '7'),
       (-8L, 'testOther', '6', '2023-06-25', '2023-06-29', '8'),
       (-9L, 'testFestival2', '2', '2023-08-10', '2023-008-13', '9'),
       (-10L, 'testConcert2', '1', '2023-05-14', '2023-05-18', '10')
;
INSERT INTO location (id, city, country, description, name, postal_code, street)
VALUES (-1L, 'Vienna', 'Austria', 'Football Stadium with Capacity of app. 50k', 'Ernst-Happel-Stadion', '1020',
        'Prater1'),
       (-2L, 'Munich', 'Germany', 'Football Stadium with Capacity of app. 50k', 'Allianz Arena', '100000',
        'Froettmaning 2'),
       (-3L, 'test', 'test', 'cool location', 'test', '100000',
        'street1')
;
INSERT INTO performance (id, end_time, start_time, title, event_id, location_id, price)
VALUES (-1L, '2023-05-20 12:00:00', '2022-05-22 23:00:00', 'testPerformance1', -1L, -1L, 100),
       (-2L, '2022-05-20 12:00:00', '2023-05-22 23:00:00', 'testPerformance2', -2L, -1L, 100),
       (-3L, '2023-05-17 12:00:00', '2023-05-15 12:00:00', 'testPerformance3', -10L, -2L, 100)
;

INSERT INTO performance_performers (performance_id, performers)
VALUES (-1L, 'testPerformer1'),
       (-1L, 'testPerformer2'),
       (-2L, 'testPerformer3'),
       (-2L, 'testPerformer4'),
       (-2L, 'testPerformer5');

INSERT INTO application_user (id, email, password, admin, first_name, last_name, phone_number, enabled, counter)
VALUES ('90857b96-a69b-466b-90de-08c0ea4e66f4', 'test@email.com', 'test', false, 'test', 'user', '+43 198291', true, 0),
       ('ac55a452-f33d-42fe-9e85-185fc1f273ba', 'testAdmin@email.com', 'admin', true, 'admin', 'user', '+49 123456',
        true, 1);

INSERT INTO seat_map(id, name)
VALUES ('24e55098-9e8b-4d7a-b573-5dcd752a8097', 'testSeatMap1'),
       ('876af6ba-a75b-4f1b-ab6a-03f6864ee44e', 'testSeatMap2');

INSERT INTO sector (id, type, seat_map_id, name, price, orientation, lodge_size, seat_map_row, seat_map_column,
                    no_update, standing_sector, length, width)
VALUES ('ad2303fe-85fc-4132-a014-ef6a4ed4caf5', 0, '24e55098-9e8b-4d7a-b573-5dcd752a8097', 'test sector1', 1.5, 25, 0,
        0, 0, false, false, 1, 3),
       ('e0e0e0e0-85fc-4132-a014-ef6a4ed4caf5', 3, '876af6ba-a75b-4f1b-ab6a-03f6864ee44e', 'test sector3', 1.2, 75, 10,
        2, 0, false, false, 2, 2);

INSERT INTO seat (id, number, sector_id, seat_row, seat_column)
VALUES ('b1c9473d-02a1-437f-aaaf-8828e0093716', 1, 'ad2303fe-85fc-4132-a014-ef6a4ed4caf5', 0, 0),
       ('f344be21-8a26-467a-a86a-19e6ca65996e', 1, 'e0e0e0e0-85fc-4132-a014-ef6a4ed4caf5', 0, 0),
       ('a2b3de22-8a26-467a-a86a-19e6ca65996e', 2, 'e0e0e0e0-85fc-4132-a014-ef6a4ed4caf5', 0, 1);

INSERT INTO ticket (id, price, performance_id, user_id, order_id, seat_id, reserved, canceled)
VALUES (-1L, 150, -1L, 'ac55a452-f33d-42fe-9e85-185fc1f273ba', null, 'b1c9473d-02a1-437f-aaaf-8828e0093716', true, false),
       (-2L, 120, -2L, 'ac55a452-f33d-42fe-9e85-185fc1f273ba', null, 'f344be21-8a26-467a-a86a-19e6ca65996e', true, false),
       (-3L, 120, -2L, 'ac55a452-f33d-42fe-9e85-185fc1f273ba', null, 'a2b3de22-8a26-467a-a86a-19e6ca65996e', true, false);



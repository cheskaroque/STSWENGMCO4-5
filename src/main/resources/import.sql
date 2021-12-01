INSERT INTO subject (subject_id) VALUES ('PE1'), ('Psych101'), ('Econ101'), ('BA101'), ('ES1'), ('Com1'), ('Math1'), ('Math2'), ('Stat1');
INSERT INTO subject_prerequisites (subject_subject_id, prerequisites_subject_id) VALUES ('Math2', 'Math1'), ('Stat1','Math2');
INSERT INTO room (name, capacity) VALUES ('AS204', 10), ('AS105', 10), ('FC103', 10), ('ENG302', 10), ('Stat213', 10),
('BA313', 10), ('Econ123', 10), ('Psych217', 10), ('Gym', 10), ('SmallRoom', 1);
INSERT INTO admin (id, firstname, lastname) VALUES (1, 'Richard', 'Webber'), (2, 'Miranda', 'Bailey'), (3, 'Owen', 'Hunt');
INSERT INTO section (section_id, num_students_enlisted, days, start_time, end_time, room_name, subject_subject_id, version) VALUES
('ABC', 0, 0, '08:30', '10:00', 'AS204', 'Math1', 0),
('PQR', 0, 2, '16:00', '17:30', 'BA313', 'BA101', 0),
('STU', 0, 0, '08:30', '10:00', 'Econ123', 'Econ101', 0),
('DEF', 0, 1, '10:00', '11:30', 'AS105', 'Math2', 0),
('GHI', 0, 2, '11:30', '13:00', 'FC103', 'Com1', 0),
('VWX', 0, 1, '10:00', '11:30', 'Psych217', 'Psych101', 0),
('JKL', 0, 0, '13:00', '14:30', 'ENG302', 'ES1', 0),
('MNO', 0, 1, '14:30', '16:00', 'Stat213', 'Stat1', 0),
('YZA', 0, 2, '11:30', '13:00', 'Gym', 'PE1', 0);

INSERT INTO room_sections (room_name, sections_section_id) VALUES ('AS204', 'ABC'), ('BA313', 'PQR'), ('Econ123', 'STU'),
('AS105','DEF'), ('FC103', 'GHI'), ('Psych217','VWX'), ('ENG302', 'JKL'), ('Stat213', 'MNO'), ('Gym', 'YZA');

INSERT INTO student (student_number, firstname, lastname) VALUES
(1, 'Meredith', 'Grey'), (2, 'Alex', 'Karev'), (3, 'Derek', 'Shepherd');
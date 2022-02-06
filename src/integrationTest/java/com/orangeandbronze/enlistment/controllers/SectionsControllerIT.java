package com.orangeandbronze.enlistment.controllers;

import com.orangeandbronze.enlistment.domain.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;

import java.time.LocalTime;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.orangeandbronze.enlistment.domain.Days.MTH;
import static com.orangeandbronze.enlistment.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest

class SectionsControllerIT {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AdminRepository adminRepository;
    private final static String TEST = "test";

    @Container
    private final PostgreSQLContainer container = new PostgreSQLContainer("postgres:14")
            .withDatabaseName(TEST).withUsername(TEST).withPassword(TEST);

    @DynamicPropertySource
    private static void properties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", () -> "jdbc:tc:postgresql:14:///" + TEST);
        registry.add("spring.datasource.password", () -> TEST);
        registry.add("spring.datasource.username", () -> TEST);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");


    }

    @Test
    void createSection_save_to_db() throws Exception {
        // GIVEN: a `subject` and a `room` existing in the database
        jdbcTemplate.update("INSERT INTO student (student_number, firstname, lastname) VALUES (?,?,?)",
                DEFAULT_STUDENT_NUMBER, "firstname", "lastname");

        final String roomName = "defaultRoom";
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?, ?)", roomName, 10);
        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", DEFAULT_SUBJECT.toString());
        jdbcTemplate.update("INSERT INTO section (section_id, number_of_students, days, start_time, end_time, room_name, subject_subject_id) VALUES (?, ?, ?, ?, ?, ?, ?) ",
                DEFAULT_SECTION_ID, 0, Days.MTH.ordinal(), LocalTime.of(9, 0),  LocalTime.of(10, 0) , roomName, DEFAULT_SUBJECT.toString());
        jdbcTemplate.update("INSERT INTO faculty (faculty_number, first_name, last_name) VALUES (?, ?, ?)", DEFAULT_FACULTY_NUMBER, firstName, lastName);

        // WHEN: the `POST` method on path "/sections" is invoked
        // with parameters `sectionId`, `subjectId`, `roomName`, `days`, `start`, `end`
        // and with an `admin` object in session that exists in the database
        Admin admin = adminRepository.findById(1).orElseThrow(() ->
                new NoSuchElementException("No admin with id number " + 1 + " found in database."));
        mockMvc.perform(post("/sections").sessionAttr("admin", admin).param(
                "sectionId", DEFAULT_SECTION_ID).param("subjectId", DEFAULT_SUBJECT.toString()).param(
                "days", String.valueOf(Days.MTH.ordinal())).param(
                "start", String.valueOf(LocalTime.of(9,0))).param(
                "end", String.valueOf(LocalTime.of(10, 0))).param(
                "roomName", roomName).param("facultyNumber", String.valueOf(DEFAULT_FACULTY_NUMBER)));

        // THEN: a new `section` is added to the database
        int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM section WHERE section_id = ? AND number_of_students = ? AND " +
                        "days = ? AND end_time = ? AND start_time = ? AND version = ? AND room_name = ? AND " +
                        "subject_subject_id = ?",
                Integer.class, DEFAULT_SECTION_ID, 0, Days.MTH.ordinal(),
                LocalTime.of(10, 0),  LocalTime.of(9, 0),
                0, roomName, DEFAULT_SUBJECT.toString()
        );

        assertEquals(1, count);
    }
}

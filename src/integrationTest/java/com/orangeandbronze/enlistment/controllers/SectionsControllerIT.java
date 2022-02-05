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
        final String start  = "09:00";
        final String end  = "10:00";
        final String days = "days";
        final String roomName = "defaultRoom";

        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?,?)", roomName, 10);
        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", DEFAULT_SUBJECT_ID);
        jdbcTemplate.update("INSERT INTO admin (id, firstname, lastname) VALUES (?,?,?)", DEFAULT_ADMIN_ID, "firstname", "lastname");
        jdbcTemplate.update("INSERT INTO faculty (faculty_number) VALUES (?)", DEFAULT_FACULTY_NUMBER);
        Admin admin = adminRepository.findById(DEFAULT_ADMIN_ID).orElseThrow(() ->
                new NoSuchElementException("No admin w/ admin ID " + DEFAULT_ADMIN_ID + " found in DB."));

        mockMvc.perform((post("/sections")).sessionAttr("admin", admin)
                .param("sectionId", DEFAULT_SECTION_ID).param("subjectId", DEFAULT_SUBJECT_ID)
                .param(days, "MTH").param("start", start).param("end", end).param("roomName", roomName).param("facultyNumber", String.valueOf(DEFAULT_FACULTY_NUMBER)));

        Map<String, Object> results = jdbcTemplate.queryForMap("SELECT * FROM section WHERE section_id = ?", DEFAULT_SECTION_ID);

        assertAll(
                () -> assertEquals(DEFAULT_SECTION_ID, results.get("section_id")),
                () -> assertEquals(DEFAULT_SUBJECT_ID, results.get("subject_subject_id")),
                () -> assertEquals(MTH.ordinal(), results.get("days")),
                () -> assertEquals(LocalTime.parse(start), LocalTime.parse(results.get("start_time").toString())),
                () -> assertEquals(LocalTime.parse(end), LocalTime.parse(results.get("end_time").toString())),
                () -> assertEquals(roomName, results.get("room_name")),
                () -> assertEquals(DEFAULT_FACULTY_NUMBER, results.get("instructor_faculty_number"))
        );

    }

}
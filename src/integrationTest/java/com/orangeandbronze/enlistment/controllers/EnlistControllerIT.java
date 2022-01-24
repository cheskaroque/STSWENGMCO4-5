package com.orangeandbronze.enlistment.controllers;

import com.orangeandbronze.enlistment.domain.*;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static com.orangeandbronze.enlistment.controllers.UserAction.*;
import static com.orangeandbronze.enlistment.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@SpringBootTest

class EnlistControllerIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StudentRepository studentRepository;

    private final static String TEST = "test";

    @Container
    private final PostgreSQLContainer container = new PostgreSQLContainer("postgres:14").withDatabaseName(TEST).withPassword(TEST);

    @DynamicPropertySource
    private static void properties (DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:tc:postgresql:14:///" + TEST);
        registry.add("spring.datasource.password", () -> TEST);
        registry.add("spring.datasource.username", () -> TEST);

    }



    @Test
    void enlist_student_in_section() throws Exception {
        jdbcTemplate.update("INSERT INTO student (student_number, firstname, lastname) VALUES (?,?,?)", DEFAULT_STUDENT_NUMBER, "firstname", "lastname");
        final String roomName = "defaultRoom";
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?,?)", roomName, 10);
        jdbcTemplate.update("INSERT INTO subject(subject_id) VALUES(?)", DEFAULT_SUBJECT.toString());
        jdbcTemplate.update("INSERT INTO section (section_id,number_of_students, days, start_time, end_time, room_name, subject_subject_id)" +
              " VALUES (?,?,?,?,?,?,?)", DEFAULT_SECTION_ID, 0, Days.MTH.ordinal(), LocalTime.of(9,0), LocalTime.of(10,0), roomName,
                DEFAULT_SUBJECT.toString());

        Student student = studentRepository.findById(DEFAULT_STUDENT_NUMBER).orElseThrow( ()->
                new NoSuchElementException("No student w/ student num " + DEFAULT_STUDENT_NUMBER + " found in DB."));
        mockMvc.perform(post("/enlist").sessionAttr("student", student).param("sectionId", DEFAULT_SECTION_ID).param("userAction", ENLIST.name()));

         int count =  jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_sections WHERE student_student_number = ? AND sections_section_id = ?",
                Integer.class, DEFAULT_STUDENT_NUMBER, DEFAULT_SECTION_ID
        );

         assertEquals(1, count);

    }

    @Test
    void cancel_student_in_section() throws Exception {
        jdbcTemplate.update("INSERT INTO student (student_number, firstname, lastname) VALUES (?,?,?)", DEFAULT_STUDENT_NUMBER, "firstname", "lastname");
        final String roomName = "defaultRoom";
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?,?)", roomName, 10);
        jdbcTemplate.update("INSERT INTO subject(subject_id) VALUES(?)", DEFAULT_SUBJECT.toString());
        jdbcTemplate.update("INSERT INTO section (section_id,number_of_students, days, start_time, end_time, room_name, subject_subject_id)" +
                        " VALUES (?,?,?,?,?,?,?)", DEFAULT_SECTION_ID, 0, Days.MTH.ordinal(), LocalTime.of(9,0), LocalTime.of(10,0), roomName,
                DEFAULT_SUBJECT.toString());
        jdbcTemplate.update("INSERT INTO student_sections (student_student_number, sections_section_id)" + "VALUES (?,?)", DEFAULT_STUDENT_NUMBER, DEFAULT_SECTION_ID);
        Student student = studentRepository.findById(DEFAULT_STUDENT_NUMBER).orElseThrow( ()->
                new NoSuchElementException("No student w/ student num " + DEFAULT_STUDENT_NUMBER + " found in DB."));
        mockMvc.perform(post("/enlist").sessionAttr("student", student).param("sectionId", DEFAULT_SECTION_ID).param("userAction", CANCEL.name()));

        int count =  jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_sections WHERE student_student_number = ? AND sections_section_id = ?",
                Integer.class, DEFAULT_STUDENT_NUMBER, DEFAULT_SECTION_ID
        );

        assertEquals(0, count);

    }

}

package com.orangeandbronze.enlistment.controllers;

import com.orangeandbronze.enlistment.domain.Days;
import com.orangeandbronze.enlistment.domain.Student;
import com.orangeandbronze.enlistment.domain.StudentRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.*;
import org.springframework.jdbc.core.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.PipedOutputStream;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;

import static com.orangeandbronze.enlistment.controllers.UserAction.*;
import static com.orangeandbronze.enlistment.domain.TestUtils.*;
import static org.junit.Assert.assertEquals;
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
    private final PostgreSQLContainer container = new PostgreSQLContainer("postgres:14")
            .withDatabaseName(TEST).withUsername(TEST).withPassword(TEST);

    @DynamicPropertySource
    private static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:tc:postgresql:14:///" + TEST);
        registry.add("spring.datasource.password", () -> TEST);
        registry.add("spring.datasource.username", () -> TEST);
    }

    @Test
    void enlist_student_in_section() throws Exception {
        jdbcTemplate.update("INSERT INTO student (student_number, firstname, lastname) VALUES (?,?,?)", DEFAULT_STUDENT_NUMBER, "firstname", "lastname");

        final String roomName = "defaultRoom";
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?, ?)", roomName, 10);
        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", DEFAULT_SUBJECT.toString());
        jdbcTemplate.update(
                "INSERT INTO section(section_id, number_of_students, days, start_time, end_time, room_name, subject_subject_id) " +
                        " VALUES (?, ?, ?, ?, ?, ?, ?)", DEFAULT_SECTION_ID, 0, Days.MTH.ordinal(), LocalTime.of(9,0), LocalTime.of(10,0), roomName, DEFAULT_SUBJECT.toString());

        Student student = studentRepository.findById(DEFAULT_STUDENT_NUMBER).orElseThrow(() ->
                new NoSuchElementException("No student w/ student num " + DEFAULT_STUDENT_NUMBER + " found in DB."));
        mockMvc.perform(post("/enlist").sessionAttr("student", student).param("sectionId", DEFAULT_SECTION_ID)
                .param("userAction", ENLIST.name()));

        int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_sections WHERE student_student_number = ? AND sections_section_id = ?",
                Integer.class, DEFAULT_STUDENT_NUMBER, DEFAULT_SECTION_ID
        );

        assertEquals(1, count);
    }

    @Test
    void cancel_student_in_section() throws Exception {
        jdbcTemplate.update("INSERT INTO student (student_number, firstname, lastname) VALUES (?,?,?)",
                DEFAULT_STUDENT_NUMBER, "firstname", "lastname");

        final String roomName = "defaultRoom";
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?, ?)", roomName, 10);
        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", DEFAULT_SUBJECT.toString());
        jdbcTemplate.update(
                "INSERT INTO section(section_id, number_of_students, days, start_time, end_time, room_name, subject_subject_id) " +
                        " VALUES (?, ?, ?, ?, ?, ?, ?)",
                DEFAULT_SECTION_ID, 0, Days.MTH.ordinal(), LocalTime.of(9,0), LocalTime.of(10,0), roomName, DEFAULT_SUBJECT.toString());
        jdbcTemplate.update("INSERT INTO student_sections (student_student_number, sections_section_id) "
                + " VALUES (?, ?)", DEFAULT_STUDENT_NUMBER, DEFAULT_SECTION_ID);

        Student student = studentRepository.findById(DEFAULT_STUDENT_NUMBER).orElseThrow(() ->
                new NoSuchElementException("No student w/ student num " + DEFAULT_STUDENT_NUMBER + " found in DB."));
        mockMvc.perform(post("/enlist").sessionAttr("student", student).param("sectionId", DEFAULT_SECTION_ID)
                .param("userAction", CANCEL.name()));

        int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_sections WHERE student_student_number = ? AND sections_section_id = ?",
                Integer.class, DEFAULT_STUDENT_NUMBER, DEFAULT_SECTION_ID
        );

        assertEquals(0, count);

    }

    private final static int FIRST_STUDENT_ID = 11;
    private final static int NUMBER_OF_STUDENTS = 20;
    private final static int LAST_STUDENT_NUMBER = FIRST_STUDENT_ID + NUMBER_OF_STUDENTS - 1;

    @Test
    void enlist_concurrent_separate_section_instances_representing_same_record_students_beyond_capacity() throws Exception{
        List<Object[]> batchArgs = new ArrayList<>();
        for(int i = FIRST_STUDENT_ID; i <= LAST_STUDENT_NUMBER; i++){
            batchArgs.add(new Object[]{i, "firstname", "lastname"});
        }
        jdbcTemplate.batchUpdate("INSERT INTO student(student_number, firstname, lastname) VAlUES (?, ?, ?)", batchArgs);

        final int CAPACITY = 1;

        final String roomName = "roomName";
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?, ?)", roomName, CAPACITY);
        final String subjectId = "defaultSubject";
        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", subjectId);
        jdbcTemplate.update(
                "INSERT INTO section (section_id, number_of_students, days, start_time, end_time, room_name, subject_subject_id)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?)", DEFAULT_SECTION_ID, 0, Days.MTH.ordinal(), LocalTime.of(9,0), LocalTime.of(10,0), roomName, subjectId);

        startEnlistmentThreads();

        int numStudents = jdbcTemplate.queryForObject(
                "select count(*) from student_sections where sections_section_id = '" +
                        DEFAULT_SECTION_ID + "'", Integer.class);
        assertEquals(1, numStudents);
    }

    @Test
    void enlist_concurrently_same_section_enough_capacity() throws Exception{
        List<Object[]> batchArgs = new ArrayList<>();
        for(int i = FIRST_STUDENT_ID; i <= LAST_STUDENT_NUMBER; i++){
            batchArgs.add(new Object[]{i, "firstname", "lastname"});
        }
        jdbcTemplate.batchUpdate("INSERT INTO student(student_number, firstname, lastname) VAlUES (?, ?, ?)", batchArgs);

        final int CAPACITY = NUMBER_OF_STUDENTS;

        final String roomName = "roomName";
        jdbcTemplate.update("INSERT INTO room (name, capacity) VALUES (?, ?)", roomName, CAPACITY);
        final String subjectId = "defaultSubject";
        jdbcTemplate.update("INSERT INTO subject (subject_id) VALUES (?)", subjectId);
        jdbcTemplate.update(
                "INSERT INTO section (section_id, number_of_students, days, start_time, end_time, room_name, subject_subject_id)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?)", DEFAULT_SECTION_ID, 0, Days.MTH.ordinal(), LocalTime.of(9,0), LocalTime.of(10,0), roomName, subjectId);

        startEnlistmentThreads();

        int numStudents = jdbcTemplate.queryForObject(
                "select count(*) from student_sections where sections_section_id = '" +
                        DEFAULT_SECTION_ID + "'", Integer.class);
        assertEquals(NUMBER_OF_STUDENTS, numStudents);

    }

    private void startEnlistmentThreads() throws Exception{
        CountDownLatch latch = new CountDownLatch(1);
        for(int i = FIRST_STUDENT_ID; i <= LAST_STUDENT_NUMBER; i++){
            final int studentNo = i;
            new EnlistmentThread(studentRepository.findById(studentNo).orElseThrow(() ->
                    new NoSuchElementException("No student w/ student num " + studentNo + " found in DB. ")),
                    latch, mockMvc).start();
        }
        latch.countDown();
        Thread.sleep(5000);
    }

    private static class EnlistmentThread extends Thread{

        private final Student student;
        private final CountDownLatch latch;
        private final MockMvc mockMvc;

        public EnlistmentThread(Student student, CountDownLatch latch, MockMvc mockMvc){

            this.student = student;
            this.latch = latch;
            this.mockMvc = mockMvc;
        }

        @Override
        public void run(){
            try{
                latch.await();

            }catch(InterruptedException e){
                throw new RuntimeException(e);
            }
            try{
                mockMvc.perform(post("/enlist").sessionAttr("student", student)
                        .param("sectionId", DEFAULT_SECTION_ID).param("userAction", ENLIST.name()));
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }

}

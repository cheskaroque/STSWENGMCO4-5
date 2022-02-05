package com.orangeandbronze.enlistment.domain;

import java.time.*;
import java.util.*;

import static com.orangeandbronze.enlistment.domain.Days.*;

public class TestUtils {

    public static final Schedule MTH830to10 = new Schedule(MTH, new Period(LocalTime.of(8, 30), LocalTime.of(10, 0)));
    public static final Schedule TF830to10 = new Schedule(TF, new Period(LocalTime.of(8, 30), LocalTime.of(10, 0)));
    public static final Schedule TF10to1130 = new Schedule(TF, new Period(LocalTime.of(10, 0), LocalTime.of(11, 30)));
    public static final Subject DEFAULT_SUBJECT = new Subject("DefaultSubject");
    public static final String DEFAULT_SUBJECT_ID = "DefaultSubject";
    public static final String DEFAULT_SECTION_ID = "DefaultSection";
    public static final int DEFAULT_STUDENT_NUMBER = 10;
    public static final int DEFAULT_FACULTY_NUMBER = 1000;
    public static final Faculty DEFAULT_FACULTY = new Faculty(DEFAULT_FACULTY_NUMBER);

    public static Faculty newFaculty(int facultyNumber) {
        return new Faculty(facultyNumber);
    }

    public static final int DEFAULT_ADMIN_ID = 10;

    public static Student newStudent(int studentNumber, Collection<Section> sections) {
        return new Student(studentNumber, "x", "x", sections);
    }

    public static Student newStudent(int studentNumber, Collection<Section> sections, Collection<Subject> subjectsTaken) {
        return new Student(studentNumber, "x", "x", sections, subjectsTaken);
    }

    public static Student newStudent(int studentNumber) {
        return new Student(studentNumber, "x", "x");
    }

    public static Student newDefaultStudent() {
        return newStudent(DEFAULT_STUDENT_NUMBER);
    }

    public static Section newDefaultSection(){
        return new Section (DEFAULT_SECTION_ID, DEFAULT_SUBJECT, MTH830to10, new Room("X", 10), DEFAULT_FACULTY);
    }
}
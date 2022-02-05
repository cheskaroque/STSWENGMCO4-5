package com.orangeandbronze.enlistment.controllers;

import com.orangeandbronze.enlistment.domain.*;
import org.hibernate.Session;
import org.junit.jupiter.api.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityManager;
import java.util.Optional;

import static com.orangeandbronze.enlistment.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SectionsControllerTest {

    @Test
    void createSection_save_new_section_to_repository() {
        String sectionId = "X";
        String roomName = "X";
        String subjectId = "X";
        Days days = Days.MTH;
        String startTime = "08:30";
        String endTime = "10:00";
        Room room = new Room(roomName, 40);

        // When create section (post) method is called, use non-production repo to see if controller actually calls it
        AdminRepository adminRepository = mock(AdminRepository.class);
        SectionRepository sectionRepository = mock(SectionRepository.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        FacultyRepository facultyRepository = mock(FacultyRepository.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        // Manually set the repos
        SectionsController controller = new SectionsController();
        controller.setAdminRepo(adminRepository);
        controller.setSectionRepo(sectionRepository);
        controller.setSubjectRepo(subjectRepository);
        controller.setRoomRepo(roomRepository);
        controller.setFacultyRepository(facultyRepository);

        // Set the return values
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(DEFAULT_SUBJECT));
        when(roomRepository.findById(roomName)).thenReturn(Optional.of(room));
        when(facultyRepository.findById(DEFAULT_FACULTY_NUMBER)).thenReturn(Optional.of(DEFAULT_FACULTY));
        // Simulating the user action
        EntityManager entityManager = mock(EntityManager.class);
        Session session = mock(Session.class);
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        controller.setEntityManager(entityManager);
        String returnVal = controller.createSection(sectionId, subjectId, days, startTime, endTime, roomName,DEFAULT_FACULTY_NUMBER, redirectAttributes);

        // Retrieve the Subject object from the DB
        verify(subjectRepository).findById(subjectId);
        // Retrieve the Room object from the DB
        verify(roomRepository).findById(roomName);
        // save Section to DB
        verify(sectionRepository).save(any(Section.class));
        assertEquals("redirect:sections", returnVal);
    }
}

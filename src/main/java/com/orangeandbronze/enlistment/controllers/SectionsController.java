package com.orangeandbronze.enlistment.controllers;

import com.orangeandbronze.enlistment.domain.*;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.*;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.apache.commons.lang3.Validate.notNull;

@Controller
@RequestMapping("sections")
@SessionAttributes("admin")
class SectionsController {

    @Autowired
    private SubjectRepository subjectRepo;
    @Autowired
    private AdminRepository adminRepo;
    @Autowired
    private RoomRepository roomRepo;
    @Autowired
    private SectionRepository sectionRepo;
    private EntityManager entityManager;

    @ModelAttribute("admin")
    public Admin admin(Integer id) {
        return adminRepo.findById(id).orElseThrow(() -> new NoSuchElementException("no admin found for adminId " + id));
    }

    @GetMapping
    public String showPage(Model model, Integer id) {
        Admin admin = id == null ? (Admin) model.getAttribute("admin") :
                adminRepo.findById(id).orElseThrow(() -> new NoSuchElementException("no admin found for adminId " + id));
        model.addAttribute("admin", admin);
        model.addAttribute("subjects", subjectRepo.findAll());
        model.addAttribute("rooms", roomRepo.findAll());
        model.addAttribute("sections", sectionRepo.findAll());
        return "sections";
    }

    @Retryable
    @PostMapping
    public String createSection(@RequestParam String sectionId, @RequestParam String subjectId, @RequestParam Days days,
                                @RequestParam String start, @RequestParam String end, @RequestParam String roomName, RedirectAttributes redirectAttrs) {

        Subject subject = subjectRepo.findById(subjectId).orElseThrow(() -> new NoSuchElementException("no subject found for subjectId " + subjectId));
        Room room = roomRepo.findById(roomName).orElseThrow(() -> new NoSuchElementException("no room found for roomId" + roomName));
        DateTimeFormatter dateTime;
        dateTime = DateTimeFormatter.ofPattern("H:mm");
        Period period = new Period(LocalTime.parse(start, dateTime), LocalTime.parse(end, dateTime));
        Schedule schedule = new Schedule(days, period);
        Section section = new Section(sectionId, subject, schedule, room);
        sectionRepo.save(section);
        return "redirect:sections";
    }

    void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @ExceptionHandler(EnlistmentException.class)
    public String handleException(RedirectAttributes redirectAttrs, EnlistmentException e) {
        redirectAttrs.addFlashAttribute("sectionExceptionMessage", e.getMessage());
        return "redirect:sections";
    }

    void setSubjectRepo(SubjectRepository subjectRepo) {
        this.subjectRepo = subjectRepo;
    }

    void setSectionRepo(SectionRepository sectionRepo) {
        this.sectionRepo = sectionRepo;
    }

    void setRoomRepo(RoomRepository roomRepo) {
        this.roomRepo = roomRepo;
    }

    void setAdminRepo(AdminRepository adminRepo) {
        this.adminRepo = adminRepo;
    }

}
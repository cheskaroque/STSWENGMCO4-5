package com.orangeandbronze.enlistment.domain;

import javax.persistence.*;

import static org.apache.commons.lang3.Validate.*;

@Entity
public class Faculty {
    @Id
    private final int facultyNumber;
    private final String firstName;
    private final String lastName;

     Faculty(int facultyNumber, String firstName, String lastName) {
         isTrue(facultyNumber >= 0, "facultyNumber must be non-negative, was: " + facultyNumber);
         notNull(firstName, "First name must not be empty");
         notNull(lastName, "Last name must not be empty");
        this.facultyNumber = facultyNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public int getFacultyNumber(){
         return facultyNumber;
    }

    public String getFirstName(){return firstName;}

    public String getLastName(){return lastName;}


    @Override
    public String toString() {
         return "Faculty# " + facultyNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Faculty faculty = (Faculty) o;

        return facultyNumber == faculty.facultyNumber;
    }

    @Override
    public int hashCode() {
        return facultyNumber;
    }

    /* Do not call! For JPA only*/
    private Faculty(){
         facultyNumber = -1;
         firstName = null;
         lastName = null;
    }
}

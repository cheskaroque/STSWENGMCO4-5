package com.orangeandbronze.enlistment;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isAlphanumeric;
import static org.apache.commons.lang3.Validate.*;

class Subject {

    private final String subjectId;
    private final Collection<Subject> prerequisites = new HashSet<>();

    Subject(String subjectId, Collection<Subject> prerequisites) {
        notBlank(subjectId);
        notNull(prerequisites);
        isTrue(isAlphanumeric(subjectId), "subjectId must be alphanumeric, was: " + subjectId);
        this.subjectId = subjectId;
        this.prerequisites.addAll(prerequisites);
        this.prerequisites.removeIf(Objects::isNull);
    }

    Subject(String subjectId) {
        this(subjectId, Collections.emptyList());
    }

    @Override
    public String toString() {
        return subjectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subject subject = (Subject) o;

        return subjectId != null ? subjectId.equals(subject.subjectId) : subject.subjectId == null;
    }

    @Override
    public int hashCode() {
        return subjectId != null ? subjectId.hashCode() : 0;
    }
}

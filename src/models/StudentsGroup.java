package models;

import java.util.ArrayList;
import java.util.Objects;

public class StudentsGroup {

    private ArrayList<Student> students;
    private String groupId;

    public ArrayList<Student> getStudents() {
        return students;
    }

    public void setStudents(ArrayList<Student> students) {
        this.students = students;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "StudentsGroup{" +
                "Students=" + students +
                ", groupId='" + groupId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentsGroup that = (StudentsGroup) o;
        return Objects.equals(students, that.students) &&
                Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(students, groupId);
    }
}

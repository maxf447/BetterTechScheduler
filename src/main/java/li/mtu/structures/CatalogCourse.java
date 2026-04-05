package li.mtu.structures;

import java.util.ArrayList;

public record CatalogCourse(CourseIdentifier identifier, String name, String description, Credit credit,
                            LecRecLab lecRecLab,  ArrayList<Semester> semesters, ArrayList<Restriction> restrictions,
                            ArrayList<CourseIdentifier> corequisites, Prerequisites prerequisites) {
}

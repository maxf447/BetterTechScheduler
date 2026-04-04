package li.mtu.structures;

// Represents a single course as a prerequisite
public record PrerequisiteCourse(CourseIdentifier course, boolean concurrencyAllowed) {}

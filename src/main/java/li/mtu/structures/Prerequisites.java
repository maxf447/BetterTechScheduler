package li.mtu.structures;

import java.util.ArrayList;

// Represents a prerequisite dependency for a course
// Each component can be another instance of Prerequisites or a PrerequisiteCourse
// If the operator is AND, every component must be satisfied
// If the operator is OR, at least one component must be satisfied
public record Prerequisites(ArrayList<Object> components, PrerequisiteOperator operator) {}
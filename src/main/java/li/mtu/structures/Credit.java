package li.mtu.structures;

// Represents the credit that a course offers
public record Credit(double credits, boolean creditsVariable, int maxRepetitions,
                     boolean repeatable, boolean passOrFail) {}

package li.mtu.structures;

import java.util.ArrayList;

public record Restriction(RestrictionType type, ArrayList<String> components, RestrictionMode mode) {}
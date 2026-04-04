package li.mtu.structures;

import java.util.ArrayList;

public record Restriction(String type, ArrayList<String> components, boolean conditionRequired) {}
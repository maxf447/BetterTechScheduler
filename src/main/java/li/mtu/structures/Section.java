package li.mtu.structures;

import java.util.ArrayList;

public record Section(Term term, int CRN, CourseIdentifier course, String sequenceNumber, double credits, int capacity,
                      int enrollment, String linkedSequenceNumber, ArrayList<Faculty> faculty,
                      Meetings meetings) {}

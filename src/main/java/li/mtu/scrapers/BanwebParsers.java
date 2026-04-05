package li.mtu.scrapers;

import com.fasterxml.jackson.databind.JsonNode;
import li.mtu.structures.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;

public class BanwebParsers {

    // Parse a set of attributes
    public static ArrayList<Attribute> parseAttributes(String attributeText) {
        Element attributeData = Jsoup.parse(attributeText).getElementsByTag("section").getFirst();
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (TextNode node : attributeData.textNodes()) {
            String attr = node.text().strip();
            if (attr.equals("No Attribute information available.")) break;
            int end = attr.lastIndexOf(" ");
            String name = attr.substring(0, end);
            String code = attr.substring(end + 1);
            attributes.addLast(new Attribute(name, code));
        }
        return attributes;
    }

    // Parse a single section
    public static Section parseSection(JsonNode section) {
        Term term = new Term(section.get("term").asInt(), section.get("termDesc").asText());
        int CRN = section.get("courseReferenceNumber").asInt();
        CourseIdentifier course = new CourseIdentifier(
                section.get("courseSubject").asText(), section.get("courseNumber").asInt()
        );
        String sequenceNumber = section.get("sequenceNumber").asText();
        double credits = section.get("creditHours").asDouble();
        int capacity = section.get("maximumEnrollment").asInt();
        int enrollment = section.get("enrollment").asInt();
        String linkedSequenceNumber = section.get("linkIdentifier").asText();
        ArrayList<Faculty> faculty = parseFaculty(section.get("faculty"));
        // TODO: parse meetings
        return new Section(
                term, CRN, course, sequenceNumber, credits, capacity, enrollment, linkedSequenceNumber, faculty, null
        );
    }

    // Parse a list of faculty
    private static ArrayList<Faculty> parseFaculty(JsonNode data) {
        ArrayList<Faculty> faculty = new ArrayList<>();
        for (JsonNode node : data) {
            faculty.addLast(new Faculty(node.get("displayName").asText(), node.get("emailAddress").asText()));
        }
        return faculty;
    }
}

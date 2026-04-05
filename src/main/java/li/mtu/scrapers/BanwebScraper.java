package li.mtu.scrapers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import li.mtu.structures.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

// Scrapes section data from BANWEB
public class BanwebScraper {
    String token;
    static ObjectMapper mapper = new ObjectMapper();


    // User must visit https://banweb.mtu.edu/StudentRegistrationSelfService/ssb/plan/selectPlan and give their token
    public BanwebScraper(String token) {
        this.token = token;
    }

    public static ArrayList<Term> getTerms() {
        // Make a request to https://banweb.mtu.edu/StudentRegistrationSelfService/ssb/classSearch/getTerms
        JsonNode termData;
        try {
            termData = mapper.readTree(makeGETRequest("classSearch/getTerms", "offset=1&max=500", ""));
        }
        catch (JsonProcessingException e) { return null; }

        // Parse and add terms to list
        ArrayList<Term> terms = new ArrayList<>();
        for (JsonNode term : termData) {
            terms.addLast(new Term(term.get("code").asInt(), term.get("description").asText()));
        }
        return terms;
    }

    public ArrayList<Section> scrapeSections(Term term) {
        // Send POST to API to enable scraping
        makePOSTRequest("term/search", "mode=search", String.format("term=%d", term.code()), token);

        // Iteratively fetch sections
        int offset = 0;
        int totalCount = Integer.MAX_VALUE;
        ArrayList<Section> sections = new ArrayList<>();
        // TODO: fix not working at all
        while (offset < totalCount) {
            JsonNode sectionData;
            try {
                sectionData = mapper.readTree(makeGETRequest("searchResults/searchResults",
                        String.format("txt_term=%s&pageOffset=%s&pageMaxSize=500", term.code(), offset), token));
            }
            catch (JsonProcessingException e) { return null; }
            totalCount = sectionData.get("totalCount").asInt();

            for (JsonNode section : sectionData.get("data")) {
                System.out.println(BanwebParsers.parseSection(section));
                sections.addLast(BanwebParsers.parseSection(section));
            }
            offset += 500;
        }
        return sections;
    }

    // Scrapes the attributes for a list of catalog courses
    public static ArrayList<Course> scrapeAttributes(ArrayList<CatalogCourse> catalogCourses) {
        ArrayList<Course> courses = new ArrayList<>();
        // Iterate over every course and scrape attributes
        for (CatalogCourse course : catalogCourses){
            // Get data
            String params = String.format("term=999999&subjectCode=%s&courseNumber=%s",
                    course.identifier().subject(), course.identifier().number());
            String attributeText = makePOSTRequest("courseSearchResults/getCourseAttributes", params, "", "");
            assert attributeText != null;
            // Create new course object with attributes
            ArrayList<Attribute> attributes = BanwebParsers.parseAttributes(attributeText);
            courses.addLast(new Course(
                    course.identifier(), course.name(), course.description(), course.credit(), course.lecRecLab(),
                    course.semesters(), course.restrictions(), course.corequisites(), course.prerequisites(), attributes
            ));
        }
        return courses;
    }

    // Make a GET request to BANWEB's API
    private static String makeGETRequest(String endpoint, String params, String token) {
        String url = String.format("https://banweb.mtu.edu/StudentRegistrationSelfService/ssb/%s?%s", endpoint, params);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET()
                .setHeader("Cookie", "JSESSIONID=" + token).build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        }
        catch (Exception e) { return null; }
    }

    // Make a POST request to BANWEB's API
    private static String makePOSTRequest(String endpoint, String params, String data, String token) {
        String url = String.format("https://banweb.mtu.edu/StudentRegistrationSelfService/ssb/%s?%s", endpoint, params);
        System.out.println(data);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .setHeader("Cookie", "JSESSIONID=" + token).build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        }
        catch (Exception e) { return null; }
    }
}

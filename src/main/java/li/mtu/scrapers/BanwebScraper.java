package li.mtu.scrapers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import li.mtu.structures.Attribute;
import li.mtu.structures.CatalogCourse;
import li.mtu.structures.Course;
import li.mtu.structures.Term;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.w3c.dom.Attr;

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

    public ArrayList<Term> getTerms() {
        // Make a request to https://banweb.mtu.edu/StudentRegistrationSelfService/ssb/classSearch/getTerms
        JsonNode termData;
        try {
            termData = mapper.readTree(makeGETRequest("classSearch/getTerms", "offset&1&max=500", token));
        }
        catch (JsonProcessingException e) { return null; }

        // Parse and add terms to list
        ArrayList<Term> terms = new ArrayList<>();
        for (JsonNode term : termData) {
            terms.addLast(new Term(term.get("code").asInt(), term.get("description").asText()));
        }
        return terms;
    }

    // TODO: Scrape the sections for a term
    public ArrayList<Object> scrapeSections(Term term) {
        // Send POST to https://banweb.mtu.edu/StudentRegistrationSelfService/ssb/term/search?mode=search to enable scraping
        makePOSTRequest("term/search", "mode=search", String.format("term=%d", term.code()), token);

        // Send GETs to https://banweb.mtu.edu/StudentRegistrationSelfService/ssb/searchResults/searchResults?txt_term=202608&pageOffset=0&pageMaxSize=500

        return null;
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

            // Parse data
            ArrayList<Attribute> attributes = new ArrayList<>();
            Element attributeData = Jsoup.parse(attributeText).getElementsByTag("section").getFirst();

            // Iterate and save attributes
            for (TextNode node : attributeData.textNodes()) {
                String attr = node.text().strip();
                if (attr.equals("No Attribute information available.")) break;
                int end = attr.lastIndexOf(" ");
                String name = attr.substring(0, end);
                String code = attr.substring(end + 1);
                attributes.addLast(new Attribute(name, code));
                System.out.println(attributes.getLast());
            }

            // Create new course object with attributes
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
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .setHeader("Cookie", "JSESSIONID=" + token).build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        }
        catch (Exception e) { return null; }
    }
}

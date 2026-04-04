package li.mtu.scrapers;

import li.mtu.structures.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

// Scrapes the main course catalog
public class CatalogScraper {
    private static final String UNDERGRADUATE_CATALOG_URL = "https://www.banweb.mtu.edu/pls/owa/stu_ctg_utils.p_online_all_courses_ug";
    private static final String GRADUATE_CATALOG_URL = "https://www.banweb.mtu.edu/pls/owa/stu_ctg_utils.p_online_all_courses_gr";

    // Scrape both catalogs
    public static ArrayList<Course> scrapeCatalogs() {
        ArrayList<Course> catalog = new ArrayList<>();
        catalog.addAll(scrapeCatalog(UNDERGRADUATE_CATALOG_URL));
        catalog.addAll(scrapeCatalog(GRADUATE_CATALOG_URL));
        return catalog;
    }

    private static ArrayList<Course> scrapeCatalog(String url) {
        // Download catalog
        Document catalog;
        try { catalog = Jsoup.connect(url).get(); }
        catch (IOException e) { return null; }

        // Parse each course in catalog
        Element article = catalog.getElementById("content_body");
        if (article == null) return null;

        // Output array for courses
        ArrayList<Course> courses = new ArrayList<>();

        // Properties we'll read from each course
        CourseIdentifierName name = null;
        String courseDescription = null;
        Credit credit = null;
        LecRecLab lecRecLab = null;
        ArrayList<Semester> semesters = null;
        // These are empty arrays by default since if not found it means we have none of them
        ArrayList<CourseIdentifier> corequisites = new ArrayList<>();
        ArrayList<Restriction> restrictions = new ArrayList<>();

        for (Element element : article.children()) {
            switch (element.tag().name()) {
                // h4: Course subject, number, and name
                case "h4" -> name = CatalogParsers.parseName(element.text());
                // p: Course description
                case "p" -> courseDescription = element.text();
                // ul: Course attributes
                case "ul" -> {
                    // Skip if this ul isn't an attributes ul
                    if (!element.className().equals("none")) continue;

                    // Iterate over each attribute
                    for (Element attr : element.children()) {
                        String text = attr.textNodes().getLast().text();
                        // First element contains identifier for attribute
                        switch (attr.children().getFirst().text()) {
                            case "Credits:" -> credit = CatalogParsers.parseCredit(text);
                            case "Lec-Rec-Lab:" -> lecRecLab = CatalogParsers.parseLecRecLab(text);
                            case "Semesters Offered:" -> semesters = CatalogParsers.parseSemesters(text);
                            case "Restrictions:" -> restrictions = CatalogParsers.parseRestrictions(text);
                            case "Co-Requisite(s):" -> corequisites = CatalogParsers.parseCorequisites(text);
                            case "Pre-Requisite(s):" -> {} // TODO: prerequisites
                        }
                    }

                    // ul is the final element; all of these should be populated by the time it's done being read
                    assert name != null && courseDescription != null && credit != null && semesters != null;

                    // Create course
                    courses.addLast(new Course(
                            name.identifier(),
                            name.name(),
                            courseDescription,
                            credit,
                            lecRecLab,
                            semesters,
                            restrictions));

                    // Reset properties
                    name = null;
                    courseDescription = null;
                    credit = null;
                    lecRecLab = null;
                    semesters = null;
                    corequisites = new ArrayList<>();
                    restrictions = new ArrayList<>();

                }
            }
        }
        return courses;
    }

    protected record CourseIdentifierName(CourseIdentifier identifier, String name) {}
}

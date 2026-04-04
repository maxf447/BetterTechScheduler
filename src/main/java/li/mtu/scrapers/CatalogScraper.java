package li.mtu.scrapers;

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
    public static boolean scrapeCatalogs() {
        return scrapeCatalog(UNDERGRADUATE_CATALOG_URL) && scrapeCatalog(GRADUATE_CATALOG_URL);
    }

    public static boolean scrapeCatalog(String url) {
        // Download catalog
        Document catalog;
        try { catalog = Jsoup.connect(url).get(); }
        catch (IOException e) { return false; }

        // Parse each course in catalog
        Element article = catalog.getElementById("content_body");
        if (article == null) return false;

        // Properties we'll read from each course
        CourseLabel name = null;
        String courseDescription = null;
        Credits credits = null;
        LecRecLab lecRecLab = null;
        Semesters semesters = null;
        // These are empty arrays by default since if not found it means we have none of them
        ArrayList<CourseLabelShort> corequisites = new ArrayList<>();
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
                            case "Credits:" -> credits = CatalogParsers.parseCredits(text);
                            case "Lec-Rec-Lab:" -> lecRecLab = CatalogParsers.parseLecRecLab(text);
                            case "Semesters Offered:" -> semesters = CatalogParsers.parseSemesters(text);
                            case "Restrictions:" -> restrictions = CatalogParsers.parseRestrictions(text);
                            case "Co-Requisite(s):" -> corequisites = CatalogParsers.parseCorequisites(text);
                            case "Pre-Requisite(s):" -> {} // TODO: prerequisites
                        }
                    }

                    System.out.printf("Course name: %s\n", name);
                    System.out.printf("Description: %s\n", courseDescription);
                    System.out.printf("Credits: %s\n", credits);
                    System.out.printf("LecRecLab: %s\n", lecRecLab);
                    System.out.printf("Semesters: %s\n", semesters);
                    System.out.printf("Corequisites: %s\n", corequisites);
                    System.out.printf("Restrictions: %s\n", restrictions);

                    // ul is the final element; all of these should be populated by the time it's done being read
                    assert name != null && courseDescription != null && credits != null && semesters != null &&
                            corequisites != null && restrictions != null;

                    // Reset properties
                    name = null;
                    courseDescription = null;
                    credits = null;
                    lecRecLab = null;
                    semesters = null;
                    corequisites = new ArrayList<>();
                    restrictions = new ArrayList<>();

                    // TODO: Construct a course object and save somewhere
                }
            }
        }
        return true;
    }

    protected record CourseLabel(String subject, String number, String name) {}
    protected record Credits(double credits, boolean creditsVariable, int maxRepetitions,
                             boolean repeatable, boolean passOrFail) {}
    protected record LecRecLab(double lectures, double recitations, double labs) {}
    protected record Semesters(boolean fallOdd, boolean springOdd, boolean summerOdd, boolean onDemandOdd,
                               boolean fallEven, boolean springEven, boolean summerEven, boolean onDemandEven) {}
    protected record CourseLabelShort(String subject, String number) {}
    protected record Restriction(RestrictionType type, ArrayList<String> components, boolean conditionRequired) {}

    protected enum RestrictionType { CLASS_RESTRICTION, MAJOR_RESTRICTION, LEVEL_RESTRICTION, COLLEGE_RESTRICTION,
                                     CAMPUS_RESTRICTION, INSTRUCTOR_PERMISSION, DEPARTMENT_PERMISSION,
                                     INSTRUCTOR_AND_DEPARTMENT_PERMISSION }
}

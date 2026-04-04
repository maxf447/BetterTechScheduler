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
        try {
            catalog = Jsoup.connect(url).get();
        }
        catch (IOException e) {
            return false;
        }

        // Parse each course in catalog
        Element article = catalog.getElementById("content_body");
        if (article == null) return false;
        for (Element element : article.children()) {

            CourseName name;
            String courseDescription;
            Credits credits;
            LecRecLab lecRecLab;
            Semesters semesters;
            ArrayList<Course> corequisites;

            switch (element.tag().name()) {
                // h4: Course subject, number, and name
                case "h4" -> { name = CatalogParsers.parseName(element.text()); }
                // p: Course description
                case "p" -> { courseDescription = element.val(); }
                // ul: Course attributes
                case "ul" -> {
                    // Iterate over each attribute
                    for (Element attr : element.children()) {
                        String text = attr.textNodes().getLast().text();
                        // First element contains identifier for attribute
                        switch (attr.children().getFirst().text()) {
                            case "Credits:" -> { credits = CatalogParsers.parseCredits(text); }
                            case "Lec-Rec-Lab:" -> { lecRecLab = CatalogParsers.parseLecRecLab(text); }
                            case "Semesters Offered:" -> { semesters = CatalogParsers.parseSemesters(text); }
                            case "Restrictions:" -> { System.out.println(text); }
                            case "Co-Requisite(s):" -> { corequisites = CatalogParsers.parseCorequisites(text); }
                            case "Pre-Requisite(s):" -> {}
                        }
                    }

                    // This is the last element in each course list, so we can now save the course
                }
            }
        }
        return true;
    }

    protected record CourseName(String subject, String number, String name) {};
    protected record Credits(double credits, boolean creditsVariable, int maxRepetitions,
                             boolean repeatable, boolean passOrFail) {};
    protected record LecRecLab(int lectures, int recitations, int labs) {};
    protected record Semesters(boolean fallOdd, boolean springOdd, boolean summerOdd, boolean onDemandOdd,
                               boolean fallEven, boolean springEven, boolean summerEven, boolean onDemandEven) {};
    protected record Course(String subject, String number) {};
}

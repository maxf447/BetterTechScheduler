package li.mtu.scrapers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Parsers for the catalog scraper
public class CatalogParsers {

    // Helper function to quickly parse a string for a regex match string
    private static String parse(String pattern, String string) {
        Matcher matcher = Pattern.compile(pattern).matcher(string);
        if (matcher.find()) return matcher.group(0);
        else return null;
    }

    // Helper function to check if a pattern exists
    private static boolean exists(String pattern, String string) {
        Matcher matcher = Pattern.compile(pattern).matcher(string);
        return matcher.find();
    }

    // Course subject, number, and name
    // Format: "MA 3160 - Multivariable Calculus with Technology"
    protected static CatalogScraper.CourseName parseName(String name) {
        return new CatalogScraper.CourseName(
            parse("^[A-Z]{2,4}", name),    // Matches course subject code: "MA"
            parse("[0-9]{4}", name),       // Matches course number: "3160"
            parse("(?<= - ).+$", name));   // Matches course name: " Multivariable Calculus with Technology"
    }

    // Course credits
    // Format: "<variable to 3.0|3.0>; [Repeatable to a max of 3|May be repeated]; [Graded Pass/Fail Only]"
    protected static CatalogScraper.Credits parseCredits(String credits) {
        String creditCount = parse("[0-9]+\\.[0-9]", credits);                      // Matches credits: "3.0"
        boolean creditsVariable = exists("variable to", credits);                   // Matches "variable to" anywhere
        boolean repeatable = exists("(May be repeated|Repeatable)", credits);       // Matches "May be repeated" or "Repeatable" anywhere
        String repetitions = parse("(?<=Repeatable to a Max of )[0-9]+", credits);  // Matches number after "Repeatable to a Max of "
        boolean passOrFail = exists("Graded Pass/Fail Only", credits);              // Matches "Graded Pass/Fail Only" text

        return new CatalogScraper.Credits(
            creditCount != null ? Double.parseDouble(creditCount) : 0,
            creditsVariable,
            repetitions != null ? Integer.parseInt(repetitions) : 0,
            repeatable,
            passOrFail);
    }

    // Lec-Rec-Lab count breakdown
    // Format: "(3-0-0)"
    protected static CatalogScraper.LecRecLab parseLecRecLab(String lecRecLab) {
        String lec = parse("(?<=\\()[0-9](?=,)", lecRecLab);    // Matches first component
        String rec = parse("(?<=,)[0-9](?=,)", lecRecLab);      // Matches second component
        String lab = parse("(?<=,)[0-9](?=\\))", lecRecLab);    // Matches third component

        return new CatalogScraper.LecRecLab(
            lec != null ? Integer.parseInt(lec) : 0,
            rec != null ? Integer.parseInt(rec) : 0,
            lab != null ? Integer.parseInt(lab) : 0);
    }

    // Semesters a course is offered
    // Format: "<Fall|Spring|Summer>[, in <odd|even> years]" (repeated for each semester)
    protected static CatalogScraper.Semesters parseSemesters(String semesters) {
        boolean fall = exists("Fall", semesters);
        boolean spring = exists("Spring", semesters);
        boolean summer = exists("Summer", semesters);
        boolean onDemand = exists("On Demand", semesters);

        boolean fallOddOnly = exists("Fall, in odd years", semesters);
        boolean springOddOnly = exists("Spring, in odd years", semesters);
        boolean summerOddOnly = exists("Summer, in odd years", semesters);
        boolean onDemandOddOnly = exists("On Demand, in odd years", semesters);

        boolean fallEvenOnly = exists("Fall, in even years", semesters);
        boolean springEvenOnly = exists("Spring, in even years", semesters);
        boolean summerEvenOnly = exists("Summer, in even years", semesters);
        boolean onDemandEvenOnly = exists("On Demand, in even years", semesters);

        return new CatalogScraper.Semesters(
                fall && !fallEvenOnly,
                spring && !springEvenOnly,
                summer && !summerEvenOnly,
                onDemand && !onDemandEvenOnly,
                fall && !fallOddOnly,
                spring && !springOddOnly,
                summer && !summerOddOnly,
                onDemand && !onDemandOddOnly);
    }

    // Corequisites a course has
    // Format: "MA 2160, MA 1160, [...]"
    protected static ArrayList<CatalogScraper.Course> parseCorequisites(String text) {
        Matcher subjectMatcher = Pattern.compile("[A-Z]{2,4}").matcher(text);
        Matcher numberMatcher = Pattern.compile("[0-9]{4}").matcher(text);
        ArrayList<CatalogScraper.Course> courses = new ArrayList<>();

        // Iterate over all matches
        while (subjectMatcher.find() && numberMatcher.find()) {
            courses.addLast(new CatalogScraper.Course(
                subjectMatcher.group(),
                numberMatcher.group()));
        }
        return courses;
    }
}

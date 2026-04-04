package li.mtu.scrapers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Parsers for the catalog scraper
public class CatalogParsers {

    // Helper function to quickly parse a string for a regex match string
    private static String parse(String pattern, String string) {
        Matcher matcher = Pattern.compile(pattern).matcher(string);
        if (!matcher.find()) return null;
        return matcher.group(0);
    }

    // Helper function to check if a pattern exists
    private static boolean exists(String pattern, String string) {
        Matcher matcher = Pattern.compile(pattern).matcher(string);
        return matcher.find();
    }

    // Course subject, number, and name
    // Format: "MA 3160 - Multivariable Calculus with Technology"
    protected static CatalogScraper.CourseLabel parseName(String name) {
        String subject = parse("^[A-Z]{2,4}", name);    // Matches course subject code: "MA"
        String number = parse("[0-9]{4}", name);        // Matches course number: "3160"
        String fullName = parse("(?<= - ).+$", name);  // Matches course name: " Multivariable Calculus with Technology"

        assert subject != null && number != null && fullName != null;
        return new CatalogScraper.CourseLabel(subject, number, fullName);
    }

    // Course credits
    // Format: "<variable to 3.0|3.0>; [Repeatable to a max of 3|May be repeated]; [Graded Pass/Fail Only]"
    protected static CatalogScraper.Credits parseCredits(String credits) {
        String creditCount = parse("[0-9]+\\.[0-9]", credits);                      // Matches credits: "3.0"
        boolean creditsVariable = exists("variable to", credits);                   // Matches "variable to" anywhere
        boolean repeatable = exists("(May be repeated|Repeatable)", credits);       // Matches "May be repeated" or "Repeatable" anywhere
        String repetitions = parse("(?<=Repeatable to a Max of )[0-9]+", credits);  // Matches number after "Repeatable to a Max of "
        boolean passOrFail = exists("Graded Pass/Fail Only", credits);              // Matches "Graded Pass/Fail Only" text

        assert creditCount != null;
        return new CatalogScraper.Credits(
            Double.parseDouble(creditCount),
            creditsVariable,
            repetitions != null ? Integer.parseInt(repetitions) : 0,
            repeatable,
            passOrFail);
    }

    // Lec-Rec-Lab count breakdown
    // Format: "(3-0-0)"
    protected static CatalogScraper.LecRecLab parseLecRecLab(String lecRecLab) {
        String lec = parse("(?<=\\()[0-9\\.]+(?=-)", lecRecLab);    // Matches first component
        String rec = parse("(?<=-)[0-9\\.]+(?=-)", lecRecLab);      // Matches second component
        String lab = parse("(?<=-)[0-9\\.]+(?=\\))", lecRecLab);    // Matches third component

        assert lec != null && rec != null && lab != null;
        return new CatalogScraper.LecRecLab(
            Double.parseDouble(lec),
            Double.parseDouble(rec),
            Double.parseDouble(lab));
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
    protected static ArrayList<CatalogScraper.CourseLabelShort> parseCorequisites(String text) {
        Matcher subjectMatcher = Pattern.compile("[A-Z]{2,4}").matcher(text);
        Matcher numberMatcher = Pattern.compile("[0-9]{4}").matcher(text);
        ArrayList<CatalogScraper.CourseLabelShort> courses = new ArrayList<>();

        // Iterate over all matches
        while (subjectMatcher.find() && numberMatcher.find()) {
            courses.addLast(new CatalogScraper.CourseLabelShort(
                subjectMatcher.group(),
                numberMatcher.group()));
        }

        assert !courses.isEmpty();
        return courses;
    }

    // Restrictions a course has
    // Format: "<Must|May not> be enrolled in one of the following <...>: <...>; ..."
    // Format: "Permission of <instructor|department|instructor and department> required"
    protected static ArrayList<CatalogScraper.Restriction> parseRestrictions(String text) {

        // Split on semicolons to process each restriction individually
        ArrayList<CatalogScraper.Restriction> restrictions = new ArrayList<>();
        for (String part : text.split("; ")) {
            CatalogScraper.RestrictionType type = null;
            boolean conditionRequired = true;
            ArrayList<String> components = null;

            // Permission restriction
            if (exists("Permission of instructor required", part))
                type = CatalogScraper.RestrictionType.INSTRUCTOR_PERMISSION;
            else if (exists("Permission of department required", part))
                type = CatalogScraper.RestrictionType.DEPARTMENT_PERMISSION;
            else if (exists("Permission of instructor and department required", part))
                type = CatalogScraper.RestrictionType.INSTRUCTOR_AND_DEPARTMENT_PERMISSION;

            // Other restriction
            else if (exists("(Must|May not) be enrolled in one of the following", part)) {
                conditionRequired = exists("May not be enrolled in one of the following", part);
                String typeString = parse("(?<=one of the following )[A-Za-z]+(?=\\()", part);
                assert typeString != null;
                switch (typeString) {
                    case "Class" -> type = CatalogScraper.RestrictionType.CLASS_RESTRICTION;
                    case "Major" -> type = CatalogScraper.RestrictionType.MAJOR_RESTRICTION;
                    case "Level" -> type = CatalogScraper.RestrictionType.LEVEL_RESTRICTION;
                    case "College" -> type = CatalogScraper.RestrictionType.COLLEGE_RESTRICTION;
                    case "Campus" -> type = CatalogScraper.RestrictionType.CAMPUS_RESTRICTION;
                }
                String componentText = parse("(?<=\\): ).*", part);
                assert componentText != null;
                components = new ArrayList<>(Arrays.asList(componentText.split(", ")));
            }

            assert type != null;
            restrictions.addLast(new CatalogScraper.Restriction(
                type,
                components,
                conditionRequired));
        }

        assert !restrictions.isEmpty();
        return restrictions;
    }
}

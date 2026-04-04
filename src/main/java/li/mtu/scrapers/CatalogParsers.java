package li.mtu.scrapers;

import li.mtu.structures.*;

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
    protected static CatalogScraper.CourseIdentifierName parseName(String name) {
        String subject = parse("^[A-Z]{2,4}", name);    // Matches course subject code: "MA"
        String number = parse("[0-9]{4}", name);        // Matches course number: "3160"
        String fullName = parse("(?<= - ).+$", name);  // Matches course name: " Multivariable Calculus with Technology"

        assert subject != null && number != null && fullName != null;
        return new CatalogScraper.CourseIdentifierName(
            new CourseIdentifier(subject, Integer.parseInt(number)),
            fullName);
    }

    // Course credits
    // Format: "<variable to 3.0|3.0>; [Repeatable to a max of 3|May be repeated]; [Graded Pass/Fail Only]"
    protected static Credit parseCredit(String credits) {
        String creditCount = parse("[0-9]+\\.[0-9]", credits);                      // Matches credits: "3.0"
        boolean creditsVariable = exists("variable to", credits);                   // Matches "variable to" anywhere
        boolean repeatable = exists("(May be repeated|Repeatable)", credits);       // Matches "May be repeated" or "Repeatable" anywhere
        String repetitions = parse("(?<=Repeatable to a Max of )[0-9]+", credits);  // Matches number after "Repeatable to a Max of "
        boolean passOrFail = exists("Graded Pass/Fail Only", credits);              // Matches "Graded Pass/Fail Only" text

        assert creditCount != null;
        return new Credit(
            Double.parseDouble(creditCount),
            creditsVariable,
            repetitions != null ? Integer.parseInt(repetitions) : 0,
            repeatable,
            passOrFail);
    }

    // Lec-Rec-Lab count breakdown
    // Format: "(3-0-0)"
    protected static LecRecLab parseLecRecLab(String lecRecLab) {
        String lec = parse("(?<=\\()[0-9\\.]+(?=-)", lecRecLab);    // Matches first component
        String rec = parse("(?<=-)[0-9\\.]+(?=-)", lecRecLab);      // Matches second component
        String lab = parse("(?<=-)[0-9\\.]+(?=\\))", lecRecLab);    // Matches third component

        assert lec != null && rec != null && lab != null;
        return new LecRecLab(
            Double.parseDouble(lec),
            Double.parseDouble(rec),
            Double.parseDouble(lab));
    }

    // Semesters a course is offered
    // Format: "<Fall|Spring|Summer>[, in <odd|even> years]" (repeated for each semester)
    protected static ArrayList<Semester> parseSemesters(String semesterText) {
        ArrayList<Semester> semesters = new ArrayList<>();
        boolean fall = exists("Fall", semesterText);
        boolean spring = exists("Spring", semesterText);
        boolean summer = exists("Summer", semesterText);
        boolean onDemand = exists("On Demand", semesterText);

        boolean fallOddOnly = exists("Fall, in odd years", semesterText);
        boolean springOddOnly = exists("Spring, in odd years", semesterText);
        boolean summerOddOnly = exists("Summer, in odd years", semesterText);
        boolean onDemandOddOnly = exists("On Demand, in odd years", semesterText);

        boolean fallEvenOnly = exists("Fall, in even years", semesterText);
        boolean springEvenOnly = exists("Spring, in even years", semesterText);
        boolean summerEvenOnly = exists("Summer, in even years", semesterText);
        boolean onDemandEvenOnly = exists("On Demand, in even years", semesterText);

        if (fall && !fallEvenOnly) semesters.addLast(Semester.FALL_ODD);
        if (fall && !fallOddOnly) semesters.addLast(Semester.FALL_EVEN);
        if (spring && !springEvenOnly) semesters.addLast(Semester.SPRING_ODD);
        if (spring && !springOddOnly) semesters.addLast(Semester.SPRING_EVEN);
        if (summer && !summerEvenOnly) semesters.addLast(Semester.SUMMER_ODD);
        if (summer && !summerOddOnly) semesters.addLast(Semester.SUMMER_EVEN);
        if (onDemand && !onDemandEvenOnly) semesters.addLast(Semester.ON_DEMAND_ODD);
        if (onDemand && !onDemandOddOnly) semesters.addLast(Semester.ON_DEMAND_EVEN);

        return semesters;
    }

    // Corequisites a course has
    // Format: "MA 2160, MA 1160, [...]"
    protected static ArrayList<CourseIdentifier> parseCorequisites(String text) {
        Matcher subjectMatcher = Pattern.compile("[A-Z]{2,4}").matcher(text);
        Matcher numberMatcher = Pattern.compile("[0-9]{4}").matcher(text);
        ArrayList<CourseIdentifier> courses = new ArrayList<>();

        // Iterate over all matches
        while (subjectMatcher.find() && numberMatcher.find()) {
            courses.addLast(new CourseIdentifier(
                subjectMatcher.group(),
                Integer.parseInt(numberMatcher.group())));
        }

        assert !courses.isEmpty();
        return courses;
    }

    // Restrictions a course has
    // Format: "<Must|May not> be enrolled in one of the following <...>: <...>; ..."
    // Format: "Permission of <instructor|department|instructor and department> required"
    protected static ArrayList<Restriction> parseRestrictions(String text) {

        // Split on semicolons to process each restriction individually
        ArrayList<Restriction> restrictions = new ArrayList<>();
        for (String part : text.split("; ")) {
            String type = null;
            ArrayList<String> components = null;
            boolean conditionRequired = true;

            // Permission restriction
            if (exists("Permission of instructor required", part))
                type = "PermissionInstructor";
            else if (exists("Permission of department required", part))
                type = "PermissionDepartment";
            else if (exists("Permission of instructor and department required", part))
                type = "PermissionInstructorDepartment";

            // Other restriction
            else if (exists("(Must|May not) be enrolled in one of the following", part)) {
                conditionRequired = exists("May not be enrolled in one of the following", part);
                type = parse("(?<=one of the following )[A-Za-z]+(?=\\()", part);
                String componentText = parse("(?<=\\): ).*", part);
                assert componentText != null;
                components = new ArrayList<>(Arrays.asList(componentText.split(", ")));
            }

            assert type != null;
            restrictions.addLast(new Restriction(
                type,
                components,
                conditionRequired));
        }

        assert !restrictions.isEmpty();
        return restrictions;
    }
}

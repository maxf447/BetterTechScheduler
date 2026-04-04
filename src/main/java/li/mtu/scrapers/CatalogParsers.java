package li.mtu.scrapers;

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
        System.out.println(credits);
        String creditCount = parse("[0-9]+\\.[0-9]", credits);                      // Matches credits: "3.0"
        boolean creditsVariable = exists("variable to", credits);                   // Matches "variable to" anywhere
        boolean repeatable = exists("(May be repeated|Repeatable)", credits);       // Matches "May be repeated" or "Repeatable" anywhere
        String repetitions = parse("(?<=Repeatable to a Max of )[0-9]+", credits);  // Matches number after "Repeatable to a Max of "
        boolean passOrFail = exists("Graded Pass/Fail Only", credits);             // Matches "Graded Pass/Fail Only" text

        return new CatalogScraper.Credits(
            creditCount != null ? Double.parseDouble(creditCount) : 0,
            creditsVariable,
            repetitions != null ? Integer.parseInt(repetitions) : 0,
            repeatable,
            passOrFail
        );
    }
}

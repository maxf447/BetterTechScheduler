package org.example;

import li.mtu.scrapers.BanwebScraper;
import li.mtu.scrapers.CatalogScraper;
import li.mtu.structures.CatalogCourse;
import li.mtu.structures.Course;
import li.mtu.structures.Section;
import li.mtu.structures.Term;

import java.util.ArrayList;

public class Main {
    static void main() {
//        ArrayList<CatalogCourse> catalogCourses = CatalogScraper.scrapeCatalogs();
//        ArrayList<Course> courses = BanwebScraper.scrapeAttributes(catalogCourses);
        ArrayList<Term> terms = BanwebScraper.getTerms();
        BanwebScraper scraper = new BanwebScraper("");
        ArrayList<Section> sections = scraper.scrapeSections(terms.get(1));
//        System.out.printf("Scraped %s courses\n", courses.size());
    }
}

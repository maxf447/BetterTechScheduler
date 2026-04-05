package org.example;

import li.mtu.scrapers.BanwebScraper;
import li.mtu.scrapers.CatalogScraper;
import li.mtu.structures.CatalogCourse;
import li.mtu.structures.Course;

import java.util.ArrayList;

public class Main {
    static void main() {
        ArrayList<CatalogCourse> catalogCourses = CatalogScraper.scrapeCatalogs();
        ArrayList<Course> courses = BanwebScraper.scrapeAttributes(catalogCourses);
        System.out.printf("Scraped %s courses\n", courses.size());
    }
}

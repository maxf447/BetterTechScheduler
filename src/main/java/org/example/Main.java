package org.example;

import li.mtu.scrapers.CatalogScraper;
import li.mtu.structures.Course;

import java.util.ArrayList;

public class Main {
    static void main() {
        ArrayList<Course> courses = CatalogScraper.scrapeCatalogs();
        System.out.printf("Scraped %s courses\n", courses.size());
    }
}

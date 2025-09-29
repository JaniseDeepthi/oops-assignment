package assignments;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Assignment4 {

    // A set to avoid revisiting the same URL
    private static Set<String> visited = new HashSet<>();

    // Simple robots.txt disallow rules
    private static List<String> disallowed = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        String startUrl = "https://example.com/";
        int maxPages = 5; // crawl only few pages for assignment

        // Step 1: Fetch robots.txt rules
        readRobotsTxt(startUrl);

        // Step 2: Start crawling
        crawl(startUrl, maxPages);
    }

    // Read robots.txt disallow rules
    private static void readRobotsTxt(String url) {
        try {
            URL robotsUrl = new URL(new URL(url), "/robots.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(robotsUrl.openStream()));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("Disallow:")) {
                    String path = line.split(":")[1].trim();
                    disallowed.add(path);
                }
            }
            in.close();
        } catch (Exception e) {
        	System.out.println("2117240070124 - Janise Deepthi YP");
            System.out.println("No robots.txt found, assuming no restrictions.");
            
        }
    }

    // Crawl a few pages
    private static void crawl(String startUrl, int maxPages) throws Exception {
        Queue<String> queue = new LinkedList<>();
        queue.add(startUrl);

        while (!queue.isEmpty() && visited.size() < maxPages) {
            String currentUrl = queue.poll();

            if (visited.contains(currentUrl) || isDisallowed(currentUrl)) continue;

            System.out.println("Visiting: " + currentUrl);
            visited.add(currentUrl);

            try {
                // Fetch page content
                URL urlObj = new URL(currentUrl);
                BufferedReader in = new BufferedReader(new InputStreamReader(urlObj.openStream()));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    content.append(line);
                }
                in.close();

                // Extract links (very basic regex for demo)
                String html = content.toString();
                for (String link : extractLinks(html, currentUrl)) {
                    if (!visited.contains(link)) {
                        queue.add(link);
                    }
                }

                // Step 3: Respect rate limit (sleep 2 sec between requests)
                TimeUnit.SECONDS.sleep(2);

            } catch (Exception e) {
                System.out.println("Failed to fetch: " + currentUrl);
            }
        }
    }

    // Check if a URL is disallowed by robots.txt
    private static boolean isDisallowed(String url) {
        for (String rule : disallowed) {
            if (url.contains(rule)) {
                System.out.println("Skipping (disallowed by robots.txt): " + url);
                return true;
            }
        }
        return false;
    }

    // Very basic link extractor (not robust like JSoup)
    private static List<String> extractLinks(String html, String baseUrl) {
        List<String> links = new ArrayList<>();
        String lower = html.toLowerCase();
        int index = 0;
        while ((index = lower.indexOf("href=\"", index)) != -1) {
            index += 6;
            int endIndex = lower.indexOf("\"", index + 1);
            if (endIndex > index) {
                String link = html.substring(index, endIndex);
                if (link.startsWith("http")) {
                    links.add(link);
                } else if (link.startsWith("/")) {
                    try {
                        URL base = new URL(baseUrl);
                        links.add(new URL(base, link).toString());
                    } catch (Exception e) {}
                }
            }
        }
        return links;
    }
}


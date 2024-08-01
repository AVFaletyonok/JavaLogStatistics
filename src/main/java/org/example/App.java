package org.example;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 *
 */
public class App 
{
    private static String path = ".\\src\\access.log";
    private static String outputPath = ".\\src\\result_statistics.json";
    private static DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
    private static String dateTimeRegex = "\\[([^\\]]{20}\\s\\+[0-9]{4})\\]";

    public static void main( String[] args ) throws IOException {
        Pattern dateTimePattern = Pattern.compile(dateTimeRegex);
        HashMap<Long, Integer> countPerSecond = new HashMap<>();

        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;
        int requestCount = 0;

        List<String> lines = Files.readAllLines(Paths.get(path));
        for (String line : lines) {
            Matcher matcher = dateTimePattern.matcher(line);
            if (!matcher.find()) continue;
            String dateTime = matcher.group(1);
            long time = getTimestamp(dateTime);

            minTime = Math.min(time, minTime);
            maxTime = Math.max(time, minTime);
            requestCount++;

            if (!countPerSecond.containsKey(time)) {
                countPerSecond.put(time, 0);
            }
            countPerSecond.put(time, countPerSecond.get(time) + 1);
        }
        double averageRequestPerSecond = (double)requestCount / (maxTime - minTime);
        int maxRequestPerSecond = Collections.max(countPerSecond.values());

        Statistics statistics = new Statistics(maxRequestPerSecond, averageRequestPerSecond);
        Gson gson = new Gson();
        String json = gson.toJson(statistics);

        FileWriter writer = new FileWriter(outputPath);
        writer.write(json);
        writer.flush();
        writer.close();
    }

    public static long getTimestamp(String dateTime) {
        //String dateTime = "15/Aug/2023:06:25:15 +0300";
        LocalDateTime time = LocalDateTime.parse(dateTime, formatter);
        return time.toEpochSecond(ZoneOffset.UTC);
    }
}

package engine;

import java.io.IOException;
import java.util.*;

import browsers.*;
import websites.LoginProcess;
import websites.Website;

public class SearchEngine<T> {
    private final List<Browser> browsers;

    public SearchEngine(PersonalInfo personalInfo) {
        browsers = Arrays.asList(
                new GoogleChrome(personalInfo),
                new MozillaFireFox(personalInfo),
                new Opera(personalInfo)
        );
    }

    public T handleSearch(String question, Type type, Format format) {
        QueryHolder<T> stack = new QueryHolder<>();
        String category = extractCategory(question);

        for (Browser browser : browsers)
            if (browser.supportCategory(category))
                stack.push((T) browser);

        List<String> unionList = new ArrayList<>();
        List<Number> numericResults = new ArrayList<>();

        while (!stack.isEmpty()) {
            Object item = stack.pop();
            if (item instanceof Browser browser) {
                QueryHolder<T> websites = browser.getRelevantWebsites(category);
                stack.join(websites);
            } else if (item instanceof Website site) {
                try {
                    switch (type) {
                        case INTEGER -> {
                            T value = (T) site.getAnswer(question, Integer.class);
                            if (value instanceof LoginProcess)
                                stack.push(value);
                            else {
                                Integer val = (Integer) value;
                                if (val != null) {
                                    if (format == Format.NORMAL) return (T) val;
                                    if (format == Format.AVERAGE) numericResults.add(val);
                                }
                            }
                        }
                        case DOUBLE -> {
                            T value = (T) site.getAnswer(question, Double.class);
                            if (value instanceof LoginProcess)
                                stack.push(value);
                            else {
                                Double val = (Double) value;
                                if (val != null) {
                                    if (format == Format.NORMAL) return (T) val;
                                    if (format == Format.AVERAGE) numericResults.add(val);
                                }
                            }
                        }
                        case STRING -> {
                            T value = (T) site.getAnswer(question, String.class);
                            if (value instanceof LoginProcess)
                                stack.push(value);
                            else {
                                String val = (String) value;
                                if (val != null)
                                    if (format == Format.NORMAL) return (T) val;
                            }
                        }
                        case LIST_STRING -> {
                            T value = (T) site.getAnswer(question, List.class);
                            if (value instanceof LoginProcess)
                                stack.push(value);
                            else {
                                List<String> val = (List<String>) value;
                                if (val != null) {
                                    if (format == Format.NORMAL) return (T) val;
                                    if (format == Format.UNION) {
                                        for (String v : val) {
                                            if (!unionList.contains(v))
                                                unionList.add(v);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (item instanceof LoginProcess loginTask) {
                if (loginTask.performLogin())
                    stack.push((T) loginTask.getWebsite());
            }
        }

        switch (format) {
            case UNION -> {
                return (T) unionList;
            }
            case AVERAGE -> {
                if (numericResults.isEmpty()) return null;
                double avg = numericResults.stream().mapToDouble(Number::doubleValue).average().orElse(0.0);
                if (type == Type.INTEGER)
                    return (T) Integer.valueOf((int) Math.round(avg));
                else
                    return (T) Double.valueOf(avg);
            }
            default -> {
                return null;
            }
        }
    }

    private String extractCategory(String question) {
        String str = question.toLowerCase();

        if (str.contains("loan") || str.contains("budget") || str.contains("tax") || str.contains("currency"))
            return "finance";
        if (str.contains("scholarship") || str.contains("programming") || str.contains("homework") || str.contains("lecture"))
            return "education";
        if (str.contains("software") || str.contains("robotics") || str.contains("platforms") || str.contains("installation"))
            return "technology";
        if (str.contains("goals") || str.contains("player") || str.contains("coach") || str.contains("trophy"))
            return "sports";
        if (str.contains("police") || str.contains("rubbery") || str.contains("arrest") || str.contains("gun"))
            return "crime";
        if (str.contains("actor") || str.contains("oscar") || str.contains("scene") || str.contains("imdb"))
            return "cinema";
        if (str.contains("climate") || str.contains("storm") || str.contains("rainfall") || str.contains("broadcast"))
            return "weather";
        if (str.contains("cancer") || str.contains("vaccination") || str.contains("surgery") || str.contains("strain"))
            return "health";
        return "unknown";
    }
}

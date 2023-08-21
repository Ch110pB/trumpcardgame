package org.alpacax.trumpcarddb;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UtilityClass
public class PlayerIdWebScraper {

    static final List<String> countryIds = new ArrayList<>(List.of("england-1",
            "australia-2", "south-africa-3", "west-indies-4", "new-zealand-5", "india-6",
            "pakistan-7", "sri-lanka-8", "zimbabwe-9", "united-states-of-america-11",
            "bermuda-12", "netherlands-15", "canada-17", "hong-kong-19", "papua-new-guinea-20",
            "bangladesh-25", "kenya-26", "united-arab-emirates-27", "namibia-28", "ireland-29",
            "scotland-30", "nepal-32", "oman-37", "afghanistan-40", "jersey-4083"));
    static final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    static final Set<String> countryPlayerList = map.keySet("COUNTRY-PLAYER-ID");
    static final ConcurrentHashMap<String, String> mapFinal = new ConcurrentHashMap<>();
    static final Set<String> allPlayerList = mapFinal.keySet("PLAYER-ID");
    private static final String WEB_HEADER = "https://www.espncricinfo.com/records/team/";
    private static final String ODI_FOOTER = "/one-day-internationals-2";
    private static final String[] webUrls = {"batting-most-runs-career",
            "batting-most-runs-innings", "batting-highest-career-batting-average",
            "batting-highest-career-strike-rate", "batting-most-hundreds-career",
            "batting-most-fifties-career", "bowling-most-wickets-career",
            "bowling-best-career-bowling-average", "bowling-best-career-economy-rate",
            "bowling-best-figures-innings", "keeping-most-dismissals-career",
            "fielding-most-catches-career", "individual-most-matches-career"};
    private static final Integer[][] watchlistNumbers = {
            {25, 20, 15, 15, 10, 10, 20, 15, 15, 20, 8, 10, 25},
            {20, 15, 10, 10, 8, 8, 15, 10, 10, 15, 5, 8, 20},
            {10, 8, 5, 5, 5, 5, 10, 5, 5, 8, 3, 5, 10},
            {5, 5, 3, 3, 3, 3, 5, 3, 3, 5, 2, 3, 5},
            {3, 2, 2, 2, 1, 1, 3, 1, 1, 2, 1, 2, 3}};
    private static final Integer[] countryPowerRanks = {1, 0, 1, 1, 1, 0, 0, 1, 2, 4, 4, 4,
            4, 4, 4, 2, 3, 4, 4, 3, 3, 4, 4, 3, 4};

    public static CompletableFuture<String> readWebpage(String url,
                                                        @NonNull HttpClient httpClient,
                                                        ExecutorService executor) {

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        return httpClient.sendAsync(request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApplyAsync(HttpResponse::body, executor)
                .exceptionally(ex -> null);
    }

    public static void getPlayerIds(String cn) {

        List<String> urls = new ArrayList<>();
        for (String stat : webUrls) {
            urls.add(WEB_HEADER + stat + '/' + cn + ODI_FOOTER);
        }
        HttpClient httpClient = HttpClient.newHttpClient();
        ExecutorService executor = Executors.newFixedThreadPool(urls.size());
        List<CompletableFuture<String>> futures = urls.stream()
                .map(url -> readWebpage(url, httpClient, executor)).toList();
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<String>> allPagesContent = allFutures.thenApply(
                v -> futures.stream().map(CompletableFuture::join).toList());
        allPagesContent.thenAccept(pageContents -> {
            for (int statId = 0; statId < pageContents.size(); statId++) {
                Document doc = Jsoup.parse(pageContents.get(statId));
                List<Element> playerList = doc.getElementsByClass("ds-p-0")
                        .get(0).select("table>tbody>tr>td");
                List<Element> filteredResult = new ArrayList<>();
                for (Element el : playerList) {
                    if (el.toString().contains("href=\"/cricketers")) {
                        filteredResult.add(el);
                    }
                }
                List<Element> topNResult =
                        filteredResult.subList(0,
                                Math.min(watchlistNumbers[countryPowerRanks[countryIds
                                        .indexOf(cn)]][statId], filteredResult.size()));
                List<String> playerIdList = new ArrayList<>();
                for (Element el : topNResult) {
                    playerIdList.add(el.getElementsByAttribute("href")
                            .attr("href").replace("/cricketers/", ""));
                }
                countryPlayerList.addAll(playerIdList);
            }
        }).exceptionally(ex -> null).whenComplete((v, ex) -> executor.shutdown()).join();
        allPlayerList.addAll(countryPlayerList);
    }
}

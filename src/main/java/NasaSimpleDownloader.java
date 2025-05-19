import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NasaSimpleDownloader {

    private static final String API_KEY = "QXLfir5z3SOWLXebhMTQSvHwfPMxbuGdkzRWg6bR&date=2014-10-10"; // Замените на ваш ключ
    private static final String API_URL = "https://api.nasa.gov/planetary/apod?api_key=" + API_KEY;

    public static void main(String[] args) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 1. Получаем данные от API NASA
            String mediaUrl = getMediaUrl(httpClient);

            if (mediaUrl != null) {
                // 2. Скачиваем файл
                downloadFile(httpClient, mediaUrl);
            } else {
                System.out.println("URL медиафайла не найден в ответе API");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getMediaUrl(CloseableHttpClient httpClient) throws Exception {
        HttpGet request = new HttpGet(API_URL);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.getEntity().getContent());
            EntityUtils.consume(response.getEntity());

            // Проверяем наличие url или hdurl
            if (rootNode.has("url")) {
                return rootNode.get("url").asText();
            } else if (rootNode.has("hdurl")) {
                return rootNode.get("hdurl").asText();
            }
            return null;
        }
    }

    private static void downloadFile(CloseableHttpClient httpClient, String fileUrl) throws Exception {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        String downloadDir = "nasa_downloads";

        if (!Files.exists(Paths.get(downloadDir))) {
            Files.createDirectories(Paths.get(downloadDir));
        }

        System.out.println("Скачиваем файл: " + fileName);

        HttpGet request = new HttpGet(fileUrl);
        try (CloseableHttpResponse response = httpClient.execute(request);
             InputStream inputStream = response.getEntity().getContent();
             FileOutputStream outputStream = new FileOutputStream(downloadDir + File.separator + fileName)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("Файл успешно сохранён: " + fileName);
            EntityUtils.consume(response.getEntity());
        }
    }
}
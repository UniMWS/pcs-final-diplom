import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static final int SRV_PORT = 8989;
    private static final String DIR_PDF = "pdfs";

    public static void main(String[] args) throws Exception {
        BooleanSearchEngine engine = new BooleanSearchEngine(new File(DIR_PDF));

        try (ServerSocket serverSocket = new ServerSocket(SRV_PORT);) {
            System.out.println("Сервер работает на порту " + SRV_PORT);
            while (true) {
                try (
                        Socket socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream());
                ) {

                    String word = in.readLine();
                    String response = getGson(engine, word);
                    out.write(response);
                    System.out.println(response);
                }
            }
        } catch (IOException e) {
            System.out.println("Не могу стартовать сервер");
            e.printStackTrace();
        }
    }

    private static String getGson(BooleanSearchEngine engine, String word) {
        System.out.printf("Запрос клиента: %s\n", word);
        var gson = new GsonBuilder().setPrettyPrinting().create();
        var checkSearch = engine.search(word);
        var response = gson.toJson(checkSearch.isEmpty() ? "Слово не найдено" : checkSearch);
        return response;
    }
}
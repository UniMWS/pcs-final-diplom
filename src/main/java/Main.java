import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static final int SRV_PORT = 8989;

    public static void main(String[] args) throws Exception {
        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));

        try (ServerSocket serverSocket = new ServerSocket(SRV_PORT);) {
            System.out.println("Сервер работает на порту " + SRV_PORT);
            while (true) {
                try (
                        Socket socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream());
                ) {

                    String request = in.readLine();
                    String response = getGson(engine, request);
                    out.write(response);
                    System.out.println(response);
                }
            }
        } catch (IOException e) {
            System.out.println("Не могу стартовать сервер");
            e.printStackTrace();
        }
    }

    private static String getGson(BooleanSearchEngine engine, String request) {
        System.out.printf("Запрос клиента: %s\n", request);
        var gson = new GsonBuilder().setPrettyPrinting().create();
        var checkSearch = engine.search(request) != null ? engine.search(request) : "Слово не найдено";
        var response = gson.toJson(checkSearch);
        return response;
    }
}
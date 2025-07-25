import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ConversorMoedasAPI {

    private static final String API_KEY = "83c6d2be0ee40f2decb41c60";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/USD";
    private static final String HISTORICO_ARQUIVO = "historico_conversoes.txt";

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- CONVERSOR DE MOEDAS ---");
        System.out.println("Escolha a conversão:");
        System.out.println("1. Dólar para Real");
        System.out.println("2. Euro para Real");
        System.out.println("3. Real para Dólar");
        System.out.println("4. Real para Euro");
        System.out.println("5. Dólar para Euro");
        System.out.println("6. Euro para Dólar");
        System.out.print("Opção: ");

        int opcao = scanner.nextInt();
        System.out.print("Digite o valor que deseja converter: ");
        double valor = scanner.nextDouble();

        JsonObject rates = getRates();

        double resultado = 0;
        String descricao = "";

        switch (opcao) {
            case 1:
                resultado = valor * rates.get("BRL").getAsDouble();
                descricao = "Dólar para Real";
                break;
            case 2:
                double euroToUSD = 1 / rates.get("EUR").getAsDouble();
                resultado = valor * euroToUSD * rates.get("BRL").getAsDouble();
                descricao = "Euro para Real";
                break;
            case 3:
                resultado = valor / rates.get("BRL").getAsDouble();
                descricao = "Real para Dólar";
                break;
            case 4:
                double usdToEuro = rates.get("EUR").getAsDouble();
                resultado = (valor / rates.get("BRL").getAsDouble()) * usdToEuro;
                descricao = "Real para Euro";
                break;
            case 5:
                resultado = valor * rates.get("EUR").getAsDouble();
                descricao = "Dólar para Euro";
                break;
            case 6:
                resultado = valor / rates.get("EUR").getAsDouble();
                descricao = "Euro para Dólar";
                break;
            default:
                System.out.println("Opção inválida");
                return;
        }

        System.out.printf("Valor convertido: %.2f\n", resultado);
        salvarHistorico(descricao, valor, resultado);
    }

    private static JsonObject getRates() throws IOException {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            throw new RuntimeException("Erro na requisição: " + responseCode);
        } else {
            StringBuilder inline = new StringBuilder();
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(inline.toString(), JsonObject.class);
            return json.getAsJsonObject("conversion_rates");
        }
    }

    private static void salvarHistorico(String descricao, double valorOriginal, double valorConvertido) {
        try (FileWriter writer = new FileWriter(HISTORICO_ARQUIVO, true)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            writer.write(timestamp + " - " + descricao + " - Valor original: " + valorOriginal + " - Valor convertido: " + String.format("%.2f", valorConvertido) + "\n");
        } catch (IOException e) {
            System.out.println("Erro ao salvar histórico: " + e.getMessage());
        }
    }
}

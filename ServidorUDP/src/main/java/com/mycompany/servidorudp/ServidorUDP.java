package com.mycompany.servidorudp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class ServidorUDP {
    private static final int PORT = 9876;
    private static final int MAX_USERS = 10;
    private static final int MAX_MOVIES = 20;

    // Matriz de avaliações: [usuários][filmes]
    private static final int[][] avaliacoesMatrix = new int[MAX_USERS][MAX_MOVIES];
    private static final String[] titulos = new String[MAX_MOVIES];
    private static final String[] usuarios = new String[MAX_USERS];

    public static void main(String[] args) {
        initializeSystem();

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String request = new String(packet.getData(), 0, packet.getLength()).trim();
                System.out.println("Mensagem recebida: " + request);
                
                String response = processRequest(request);
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                
                DatagramPacket responsePacket = new DatagramPacket(response.getBytes(), response.length(), clientAddress, clientPort);
                socket.send(responsePacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initializeSystem() {
        // Inicializa os filmes e usuários (Exemplo)
        titulos[0] = "Star Wars";
        titulos[1] = "Matrix";
        titulos[2] = "Inception";
        titulos[3] = "Interstellar";
        titulos[4] = "Parasita";
        // Adicione mais títulos conforme necessário
        
        usuarios[0] = "Ze";
        usuarios[1] = "Ana";
        usuarios[2] = "Martha";
        // Adicione mais usuários conforme necessário

        // Inicializa a matriz de avaliações com 0 (não avaliado)
        for (int[] row : avaliacoesMatrix) {
            Arrays.fill(row, 0);
        }
    }

    // Processa a mensagem enviada
    private static String processRequest(String request) {
        String[] parts = request.split(";");
        int command;
        try {
            command = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return "Comando inválido";
        }

        switch (command) {
            case 1:
                if (parts.length < 2) return "Parâmetros insuficientes";
                return handleRequestMovie(parts[1]);
            case 2:
                if (parts.length < 4) return "Parâmetros insuficientes";
                return handleSubmitRating(parts[1], parts[2], Integer.parseInt(parts[3]));
            case 3:
                if (parts.length < 2) return "Parâmetros insuficientes";
                return handleRecommendation(parts[1]);
            case 4:
                if (parts.length < 2) return "Parâmetros insuficientes";
                return handleListRatings(parts[1]);
            default:
                return "Comando desconhecido";
        }
    }

    // Verifica os filmes não avaliados pelo usuario
    private static String handleRequestMovie(String username) {
        int userIndex = getUserIndex(username);
        if (userIndex == -1) return "Usuário desconhecido";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_MOVIES; i++) {
            if (avaliacoesMatrix[userIndex][i] == 0 && titulos[i] != null) {
                sb.append(titulos[i]).append("; ");
            }
        }
        return sb.length() > 0 ? sb.toString() : "Todos os filmes avaliados";
    }

    // Trata a submissão de uma avaliação para um filme por um usuário.
    private static String handleSubmitRating(String username, String movieTitle, int rating) {
        int userIndex = getUserIndex(username);
        int movieIndex = getMovieIndex(movieTitle);

        if (userIndex == -1) return "Usuário desconhecido";
        if (movieIndex == -1) return "Filme desconhecido";
        if (rating < 1 || rating > 3) return "Nota inválida"; // Modificado para aceitar apenas 1, 2 ou 3

        if (avaliacoesMatrix[userIndex][movieIndex] != 0) {
            return "Filme já avaliado";
        }

        avaliacoesMatrix[userIndex][movieIndex] = rating;
        return "Avaliação registrada";
    }

    // Recomenda um filme com base nas avaliações de outros usuários.
    private static String handleRecommendation(String username) {
        int userIndex = getUserIndex(username);
        if (userIndex == -1) return "Usuário desconhecido";

        double minDistance = Double.MAX_VALUE;
        int closestUserIndex = -1;

        // Calcula a distância euclidiana entre o usuário e os outros usuários.
        for (int i = 0; i < MAX_USERS; i++) {
            if (i != userIndex && Arrays.stream(avaliacoesMatrix[i]).anyMatch(v -> v > 0)) {
                double distance = calcularDistanciaEuclideana(userIndex, i);// Calcula a distância.
                if (distance < minDistance) {
                    minDistance = distance;
                    closestUserIndex = i; // Atualiza o usuário mais próximo.
                }
            }
        }

        if (closestUserIndex == -1) return "Nenhuma recomendação disponível";

        for (int j = 0; j < MAX_MOVIES; j++) {
            if (avaliacoesMatrix[userIndex][j] == 0 && avaliacoesMatrix[closestUserIndex][j] > 0 && titulos[j] != null) {
                return titulos[j];
            }
        }

        return "Nenhuma recomendação disponível";
    }

    // Lista as avaliações de filmes feitas pelo usuário.
    private static String handleListRatings(String username) {
        int userIndex = getUserIndex(username);
        if (userIndex == -1) return "Usuário desconhecido";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_MOVIES; i++) {
            if (avaliacoesMatrix[userIndex][i] > 0 && titulos[i] != null) {
                sb.append(titulos[i]).append(": ").append(avaliacoesMatrix[userIndex][i]).append("; ");
            }
        }
        return sb.length() > 0 ? sb.toString() : "Nenhuma avaliação registrada";
    }

    // Obtém o índice do usuário com base no nome.
    private static int getUserIndex(String username) {
        for (int i = 0; i < usuarios.length; i++) {
            if (usuarios[i] != null && usuarios[i].equals(username)) {
                return i;
            }
        }
        return -1;
    }

    // Obtém o índice do filme com base no título.
    private static int getMovieIndex(String movieTitle) {
        for (int i = 0; i < titulos.length; i++) {
            if (titulos[i] != null && titulos[i].equals(movieTitle)) {
                return i;
            }
        }
        return -1;
    }

    // Calcula a distância euclidiana entre dois usuários com base nas avaliações de filmes.
    private static double calcularDistanciaEuclideana(int userIndex1, int userIndex2) {
        double sumSquaredDifferences = 0;
        for (int i = 0; i < MAX_MOVIES; i++) {
            int rating1 = avaliacoesMatrix[userIndex1][i];
            int rating2 = avaliacoesMatrix[userIndex2][i];
            if (rating1 > 0 && rating2 > 0) {
                sumSquaredDifferences += Math.pow(rating1 - rating2, 2);
            }
        }
        return Math.sqrt(sumSquaredDifferences);
    }
}

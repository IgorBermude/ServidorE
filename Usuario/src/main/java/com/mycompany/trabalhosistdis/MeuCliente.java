/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.trabalhosistdis;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MeuCliente {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

    public MeuCliente(String serverAddress, int serverPort) {
        try {
            this.serverAddress = InetAddress.getByName(serverAddress);
            this.serverPort = serverPort;
            this.socket = new DatagramSocket();
        } catch (Exception e) {
            System.err.println("Erro ao criar o cliente: " + e.getMessage());
        }
    }

    public String enviarComando(String comando) {
        try {
            // Enviar o comando para o servidor
            DatagramPacket packet = new DatagramPacket(comando.getBytes(), comando.length(), serverAddress, serverPort);
            socket.send(packet);

            // Receber a resposta do servidor
            byte[] buffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);

            // Retornar a resposta como uma String
            return new String(responsePacket.getData(), 0, responsePacket.getLength());
        } catch (Exception e) {
            return "Erro ao comunicar com o servidor: " + e.getMessage();
        }
    }

    public void fechar() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}

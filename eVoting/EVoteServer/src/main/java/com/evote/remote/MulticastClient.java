package com.evote.remote;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 * A classe MulticastClient junta-se a um grupo multicast definido ao iniciar e recebe continuamente
 * mensagens desse grupo, só agindo perante aquelas que se lhe são dirigidas, distinguindo através do
 * seu ID. Também corre uma thread MulticastUser quando necessário que é responsável pelo envio de
 * mensagens para o grupo.
 */
public class MulticastClient extends Thread {
    private static String MULTICAST_ADDRESS; //endereço multicast
    private static int PORT; //port usado pelo multicast

    private static final long id = (int)(Math.random() * 1000) + 1;
    private static String name;
    private static Boolean canVote;
    static Scanner in = new Scanner(System.in);

    long startTime;

    LinkedHashMap<String, String> parsedInput = new LinkedHashMap<>();

    public static void main(String[] args) {
        MulticastClient client = new MulticastClient(); //criar var cliente
        client.start(); //iniciar o cliente
    }

    public void run() {
        MulticastSocket socket = null; //criar var socket ainda nao atribuida
        System.out.println("Input the Multicast group address(example: 224.0.1.102): ");
        MULTICAST_ADDRESS = in.nextLine();
        startTime = System.currentTimeMillis() / 1000;
        state stateThread = new state();
        stateThread.start();

        if(readPropertiesFile()) {
            try {
                socket = new MulticastSocket(PORT); // create socket and bind it
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS); //guarda o grupo multicast na variavel group
                socket.joinGroup(group); //esta instancia multicast junta-se ao grupo

                MulticastUser user = new MulticastUser(id, name, "created", "none", "none", MULTICAST_ADDRESS);
                user.start();

                //just the part that actually receives a message and prints it
                while (true) {
                    //a função tem de receber uma mensagem com o seu id e a pedir uma operação
                    byte[] buffer = new byte[256]; //cria um bufer para receber uma mensagem
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length); //cria packet que vai ser usado como transporte da mensagem
                    socket.receive(packet); //recebe a mensagem
                    String message = new String(packet.getData(), 0, packet.getLength()); //string com a mensagem
                    parseServerInput(message);
                }
            } catch (IOException e) {
                //e.printStackTrace();
            } finally {
                if (socket != null) {
                    socket.close(); //fecha a socket no fim de tudo
                }
            }
        } else
            System.out.println("Invalid Properties File");
    }

    /**
     * Função para ler as propriedades o porto do Multicast
     * @return true caso tenha lido todos os argumentos esperados, false caso contrário
     */
    public Boolean readPropertiesFile(){
        boolean p = false;

        try {
            File myObj = new File("PropertiesFile.txt");
            Scanner reader = new Scanner(myObj);
            while (reader.hasNextLine()) {
                String[] data = reader.nextLine().split(": ");
                if(data[0].compareTo("multicastPort")==0) {
                    PORT = Integer.parseInt(data[1]);
                    p = true;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        }

        return p;
    }

    /**
     * Função usada para organizar e interpretar o conteúdo de mensagens recebidas
     * 1 - "goVote", permite ao utilizador votar no terminal e trata do processo de input do voto
     * 2 - "print", imprime no terminal informação
     * 3 - "authentication", dá começo ao processo de autenticação do utilizador no terminal
     * @param data mensagem recebida
     */
    public void parseServerInput(String data) {
        Scanner keyboardScanner = new Scanner(System.in);

        String[] aux;

        aux = data.split(";");
        for (String field: aux) {
            try {
                String[] split = field.split("\\|");
                String firstSubString = split[0].trim();
                String secondSubString = split[1].trim();
                parsedInput.put(firstSubString, secondSubString); //guarda o parsed input num hash map, ou seja, tipo de operaão coresponde à chave
            } catch (ArrayIndexOutOfBoundsException e) {}
        }
        if (parsedInput.get("id").equals(Long.toString(id))) { //verifica se a mensagem recebida é para esta maquina
            if (parsedInput.get("type") != null) { //quando o tipo de operação náo é nulo
                String type = parsedInput.get("type");
                switch (type) {
                    case "goVote":
                        if (canVote) {
                            System.out.println("Choose one of the lists: ");
                            String vote = keyboardScanner.nextLine();
                            startTime = System.currentTimeMillis() / 1000;
                            if(canVote) {
                                MulticastUser user = new MulticastUser(id, name, "vote", vote, parsedInput.get("election"), MULTICAST_ADDRESS);
                                user.start();
                                canVote = false;
                                user = new MulticastUser(id, name, "freeTerminal", "none", "none", MULTICAST_ADDRESS);
                                user.start();
                                //cleanScreen();                            //TODO put here some way to clear screen
                                System.out.println("Your vote has been registered, have a nice day!");
                            }
                            break;
                        } else { //a mensagem de error em que a autenticação falhou está na parte da autenticação em si
                            break;
                        }
                    case "print":
                        if (canVote) {
                            System.out.println(parsedInput.get("listName"));
                            startTime = System.currentTimeMillis() / 1000;
                            break;
                        }

                    case "authentication":
                        System.out.println("Welcome to the voting terminal");
                        System.out.println("Please write your name: ");
                        name = keyboardScanner.nextLine();
                        startTime = System.currentTimeMillis() / 1000;
                        System.out.println("Please write your password: ");
                        String password = keyboardScanner.nextLine();
                        startTime = System.currentTimeMillis() / 1000;
                        if (name.equals(parsedInput.get("name")) && password.equals(parsedInput.get("password"))) {
                            canVote = true; //já que ambas as autenticações foram um sucesso, pode votar
                            MulticastUser user = new MulticastUser(id, name, "sendLists", "none", "none", MULTICAST_ADDRESS); //manda uma mensagem ao servidor para este atualizar o linkedhashmap e avisar que ja se podem mandar as listas
                            user.start();
                        } else {
                            canVote = false;
                            System.out.println("Name/Password is wrong, return to voting table to re-identify");
                            MulticastUser user = new MulticastUser(id, name, "freeTerminal", "none", "none", MULTICAST_ADDRESS);
                            user.start();
                        }
                        break;
                }
            }
        }
    }

    /**
     * Thread responsável por verificar se a consola está inativa à mais de dois minutos ou
     * não, e nesse caso bloquear o terminal
     */
    private class state extends Thread {
        Thread t;

        state() {
            t = new Thread(this);
            t.start();
        }
        public void run() {
            while (true) {
                if (System.currentTimeMillis() / 1000 - startTime >= 60 && canVote) {
                    System.out.println("Terminal was idle for too long, shutting down");
                    canVote = false;
                    //MulticastUser user = new MulticastUser(id, name, "shutdown", "none", "none", MULTICAST_ADDRESS);
                    //user.start();
                    MulticastUser user = new MulticastUser(id, name, "vote", "nullVote", parsedInput.get("election"), MULTICAST_ADDRESS);
                    user.start();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }
            }
        }
    }
}

/**
 * Thread usada para enviar mensagens de volta ao servidor Multicast.
 * Para ser iniciado/criado necessita do id do cliente, do nome do utilizador atual,
 * da operação que vai pedir ao servidor Multicast, dos dados que vai transmitir a este,
 * e do endereço do grupo Multicast.
 * As operações pelas quais é responsável de informar o servidor Multicast são:
 * 1 - "vote", que signaliza a ocorrência de um voto
 * 2 - "created", que signaliza o início de um novo terminal
 * 3 - "freeTerminal", que signaliza que o terminal já não está a ser usado para votar e está livre
 * 4 - "shutdown", que signaliza que o terminal bloqueou a meio de operações
 * 5 - "sendLists", que signaliza que a autenticação foi bem sucedida e o servidor pode enviar as listas
 * candidatas da eleição escolhida para voto
 */
class MulticastUser extends Thread {
    private final String MULTICAST_ADDRESS;
    private static int PORT;

    private final long id;
    private final String name;
    private final String data; //usada para o voto enviado
    private final String operation;
    private final String dataAux; //usada para eleiçoes em caso de voto

    public MulticastUser(long id, String name, String operation, String data, String dataAux, String addr) {
        super(String.valueOf((long)(Math.random() * 1000))); //dá um nome ao user de multicast "random"
        this.id = id;
        this.name = name;
        this.data = data;
        this.operation = operation;
        this.dataAux = dataAux;
        this.MULTICAST_ADDRESS = addr;
    }

    public void run() {
        try (MulticastSocket socket = new MulticastSocket()) {
            while (true) {
                String message;
                byte[] buffer;
                InetAddress group;
                DatagramPacket packet;

                if(readPropertiesFile()) {
                    switch (operation) {
                        case "vote":
                            message = "type | vote" + "; id | " + id + "; user | " + name + "; list | " + data + "; election | " + dataAux;
                            buffer = message.getBytes();
                            group = InetAddress.getByName(MULTICAST_ADDRESS);
                            packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                            socket.send(packet);
                            socket.close();
                            break;
                        case "created":
                            message = "type | created" + "; id | " + id;
                            buffer = message.getBytes();
                            group = InetAddress.getByName(MULTICAST_ADDRESS);
                            packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                            socket.send(packet);
                            socket.close();
                            break;
                        case "freeTerminal":
                            message = "type | freeTerminal; id | " + id;
                            buffer = message.getBytes();
                            group = InetAddress.getByName(MULTICAST_ADDRESS);
                            packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                            socket.send(packet);
                            socket.close();
                            break;
                        case "shutdown":
                            message = "type | shutdown; id | " + id;
                            buffer = message.getBytes();
                            group = InetAddress.getByName(MULTICAST_ADDRESS);
                            packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                            socket.send(packet);
                            socket.close();
                            break;
                        case "sendLists":
                            message = "type | sendLists; id | " + id;
                            buffer = message.getBytes();
                            group = InetAddress.getByName(MULTICAST_ADDRESS);
                            packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                            socket.send(packet);
                            socket.close();
                            break;
                    }
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    /**
     * Função para ler as propriedades o porto do Multicast
     * @return true caso tenha lido todos os argumentos esperados, false caso contrário
     */
    public Boolean readPropertiesFile(){
        boolean p = false;

        try {
            File myObj = new File("PropertiesFile.txt");
            Scanner reader = new Scanner(myObj);
            while (reader.hasNextLine()) {
                String[] data = reader.nextLine().split(": ");
                if(data[0].compareTo("multicastPort")==0) {
                    PORT = Integer.parseInt(data[1]);
                    p = true;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        }

        return p;
    }
}
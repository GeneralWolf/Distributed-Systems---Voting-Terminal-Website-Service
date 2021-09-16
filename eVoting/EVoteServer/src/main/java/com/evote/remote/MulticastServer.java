package com.evote.remote;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.evote.models.Election;
import com.evote.models.VotingTable;
import com.evote.models.VotingTerminal;
import com.evote.models.List;
import com.evote.models.Vote;

/**
 * O MulticastServer é a classe responsável pelo funcionamento do servidor Multicast, responsável pela gerência
 * de terminais e das suas mensagens, tal como servir de ligação entre estes e o servidor RMI. Depois de iniciada e
 * configurada (submeter o endereço do grupo multicast e o nome do departamento), a configuração é autenticada e o
 * servidor fica à escuta de mensagens enquanto também fica à espera do input necessário para começar o processo de
 * voto, isto é, a identificação de um utilizador, e a escolha da eleição por este.
 */
public class MulticastServer extends Thread implements Remote {
    private static String host;
    private static String rmi_name;
    private static int rmi_port;
    private static int multicast_port;
    private static String INET_ADDR;

    static String department; //departamento em que a mesa de voto está
    static RmiServer rmi; //usar para mandar mensagens para cliente rmi/chamar os seus metodos
    static int counter = 0; //counter de pessoas que votaram na mesa
    static Boolean consoleStatus = false;
    static Boolean registered = false;
    static Boolean registeredMulticast = false;


    static CopyOnWriteArrayList<VotingTerminal> terminals = new CopyOnWriteArrayList <> ();
    static Scanner in = new Scanner(System.in);

    static VotingTable me;

    public static Handler_Msg t_handler;
    public static console handler_console;
    static LinkedHashMap<String, String> parsedInput = new LinkedHashMap<>(); //hash map para se guardar o input sempre que uma mensagem for recebida
    static LinkedHashMap<Integer, Boolean> sendTrigger = new LinkedHashMap<>(); //hash map para se saber quando já é possivel mandar as listas depois da autenticação no terminal

    public MulticastServer() throws RemoteException {
        super("Server " + (long)(Math.random() * 1000));
    }

    public static void main(String[] args) throws IOException {
        MulticastServer server = new MulticastServer();
        server.start();

        boolean exit = false;

        while (!exit) {
            if(readPropertiesFile()) {
                try {
                    rmi = (RmiServer) LocateRegistry.getRegistry(host, rmi_port).lookup(rmi_name);
                    exit = true;
                } catch (ConnectException ex) {
                    System.out.println("RMI Server is not ready yet");
                } catch (NotBoundException | RemoteException ex) {
                    //ex.printStackTrace();
                }
            }
        }

        new checkServer();

        while (!registeredMulticast) {
            System.out.println("Input the Multicast group address(example: 224.0.1.102): ");
            INET_ADDR = in.nextLine();
            boolean rmiCheck;

            while (true) {
                try {
                    rmiCheck = rmi.checkVotingTableAddress(INET_ADDR);
                    break;
                } catch (ConnectException e) {
                    //System.out.println("Error");
                }
            }

            if (rmiCheck)
                System.out.println("Address is already occupied\n");
            else
                registeredMulticast = true;
        }

        me = new VotingTable("none", true, INET_ADDR);

        while (!registered) {
            System.out.println("Input the table's department: ");
            department = in.nextLine();
            boolean exists;

            while (true) {
                try {
                    exists = rmi.checkVotingTable(department);
                    break;
                } catch (ConnectException e) {
                    //System.out.println("Error");
                }
            }

            if (exists)
                System.out.println("Voting table already exists\n");
            else
                registered = true;
        }

        me.setDepartment(department);

        try  {
            MulticastSocket socket = new MulticastSocket(4321);
            InetAddress group = InetAddress.getByName(INET_ADDR); //vai buscar o endereço do grupo ao qual se vai juntar
            socket.joinGroup(group); //junta-se ao grupo

            notifyVTState vtThreadMulticast = new notifyVTState();
            vtThreadMulticast.start();

            if (!consoleStatus) {
                handler_console = new console();
                consoleStatus = true;
            }

            while (true) {
                //System.out.println("Multicast Server ready and listening for messages...");
                byte[] buf = new byte[1000];
                DatagramPacket message = new DatagramPacket(buf, buf.length); //cria variavel para receber uma mensagem

                socket.receive(message); //recebe uma mensagem do cliente multicast
                String parsedMessage = new String(message.getData(), 0, message.getLength());
                parseUserInput(parsedMessage);
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    /**
     * Função para ler as propriedades do RMI e do Multicast, como o host, o nome e os portos do RMI e Multicast
     * @return true caso tenha lido todos os argumentos esperados, false caso contrário
     */
    public static Boolean readPropertiesFile(){
        boolean h = false, rn = false, rp = false, mp = false;

        try {
            File myObj = new File("PropertiesFile.txt");
            Scanner reader = new Scanner(myObj);
            while (reader.hasNextLine()) {
                String[] data = reader.nextLine().split(": ");
                if(data[0].compareTo("host")==0) {
                    host = data[1];
                    h = true;
                }
                if(data[0].compareTo("rmiName")==0) {
                    rmi_name = data[1];
                    rn = true;
                }
                if(data[0].compareTo("rmiPort")==0) {
                    rmi_port = Integer.parseInt(data[1]);
                    rp = true;
                }
                if(data[0].compareTo("multicastPort")==0) {
                    multicast_port = Integer.parseInt(data[1]);
                    mp = true;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        }

        return h && rn && rp && mp;
    }

    /**
     * Função responsavel por dividir e organizar os dados recebidos numa mensagem num Hashmap
     * @param data Mensagem recebida
     * @throws RemoteException no caso de as funções que usam rmi falharem
     */
    public static void parseUserInput(String data) throws RemoteException {
        String[] aux;

        aux = data.split(";");
        for (String field: aux) {
            try {
                String[] split = field.split("\\|"); //separar a chave do valor
                String firstSubString = split[0].trim(); //so retirar os espaços a mais
                String secondSubString = split[1].trim();
                parsedInput.put(firstSubString, secondSubString); //guarda o parsed input num hash map, ou seja, tipo de operaão coresponde à chave
            } catch (ArrayIndexOutOfBoundsException e) {}
        }

        if (parsedInput.get("type") != null) { //quando o tipo de operação náo é nulo
            t_handler = new Handler_Msg("new_thread", parsedInput); //cria nova thread Handler_Msg com o input processado
        }

    }

    /**
     * Thread que lida com os mensagens do MulticastClient e cria uma thread com a operação necessária
     */
    private static class Handler_Msg extends Thread { //função cria uma nova thread, a qual corre a função responsavel por escolher o tipo de operação a fazer dependente da mensagem
        Thread t;
        LinkedHashMap<String, String> input;

        Handler_Msg(String name, LinkedHashMap<String, String> parsedInput) throws RemoteException{
            t = new Thread(this, name);
            this.input = parsedInput;
            t.start(); // Start the thread
        }

        public void run() {
            try {
                operationType(input);
            } catch (RemoteException e) {
                //e.printStackTrace();
            }
        }
    }

    /**
     * Função que é responsável por decidir o que fazer com o conteúdo das mensagens recebidas dos clientes Multicast
     * As operações pelas quais é responsável de interpretar são:
     * 1 - "created", em que é notificada que um terminal se ligou a esta e atualiza as variáveis necessárias
     * 2 - "shutdown", em que atualiza o estado do terminal que enviou a mensagem para livre
     * 3 - "vote", em que regista um novo voto com a informação enviada pelo terminal. Quando o utilizador demora demasiado a votar é contado como voto nulo, e quando é escrita uma linha errada é contado como voto branco.
     * 4 - "freeTerminal", em que marca o terminal que enviou a mensagem como livre para uso
     * 5 - "sendLists", em que marca um terminal como preparado para receber as listas necessárias ao voto
     * @param parsedInput HashMap que contém os dados da mensagem organizados por chaves
     * @throws RemoteException no caso de as funções que usam rmi falharem
     */
    private static void operationType(LinkedHashMap<String, String> parsedInput) throws RemoteException {
        String type = parsedInput.get("type");

        switch (type) {
            case "created":
                terminals.add(new VotingTerminal(Integer.parseInt(parsedInput.get("id")), false));
                sendTrigger.put(Integer.parseInt(parsedInput.get("id")), false);
                counter++;
                //System.out.println("Multicast client number " + parsedInput.get("id") + " has joined the group");
                break;
            case "shutdown":
                for (int i = 0; i < terminals.size(); i++) {
                    if (terminals.get(i).getID() == Integer.parseInt(parsedInput.get("id"))) {
                        terminals.remove(i);
                        counter--;
                        break;
                    }
                }
                break;
            case "vote":
                if (parsedInput.get("list").equals("nullVote")) {
                    rmi.addVote(new Vote(rmi.getSpecificElection(parsedInput.get("election")), null, department, LocalDateTime.now(), true, false));
                    sendMsg("type | cantVote; id | " + parsedInput.get("id"), multicast_port);
                    break;
                } else {
                    List list = rmi.getList(parsedInput.get("list"));
                    if (list != null) {
                        rmi.addVote(new Vote(rmi.getSpecificElection(parsedInput.get("election")), list, department, LocalDateTime.now(), false, false));
                        rmi.addUserElection(parsedInput.get("user"), rmi.getSpecificElection(parsedInput.get("election")));
                        sendMsg("type | cantVote; id | " + parsedInput.get("id"), multicast_port);
                        break;
                    } else {
                        rmi.addVote(new Vote(rmi.getSpecificElection(parsedInput.get("election")), null, department, LocalDateTime.now(), false, true));
                        sendMsg("type | cantVote; id | " + parsedInput.get("id"), multicast_port);
                        break;
                    }
                }
            case "freeTerminal":
                freeTerminal(Integer.parseInt(parsedInput.get("id")));
                break;
            case "sendLists":
                sendTrigger.put(Integer.parseInt(parsedInput.get("id")), true);
                break;
        }
    }

    /**
     * Thread responsavel por gerir o menu de maneira ao servidor poder receber mensagens ao mesmo tempo
     */
    private static class console extends Thread {
        Thread t;
        console() throws RemoteException {
            t = new Thread(this);
            t.start();
        }

        public void run() {
            try {
                System.out.println(department + "'s Voting Table");
                showMenu();
            } catch (RemoteException | InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }

    /**
     * Função que lida com um utilizador iniciar o processo de voto.
     * Aqui é começada a primeira fase de identificação, a atribuição
     * do terminal a ser usado, e a escolha de eleição, assim como
     * a transferência dos nomes das listas candidatas da eleição escolhida
     * para o terminal (Client Multicast) e a transferência dos dados de
     * login necessários para a comparação
     * @throws RemoteException  no caso de as funções que usam rmi falharem
     * @throws InterruptedException no caso de as ordens de sleep() serem interrompidas
     *
     */
    private static void showMenu() throws RemoteException, InterruptedException {
        Election election;
        int electionIndex;
        String name, password, role;
        boolean enter;

        System.out.println("Input your name: ");
        name = in.nextLine();
        System.out.println("Input your password: ");
        password = in.nextLine();
        /* Modification Start */
        System.out.println("Input your role: ");
        role = in.nextLine();
        /* Modification End */
        while (true) {
            try {
                enter = rmi.authenticateUser(name, password, role);
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        if (enter) {
            int terminalId = chooseTerminal();
            if (terminalId != 0) {
                occupyTerminal(terminalId);
                ArrayList<Election> selectedElections;

                while (true) {
                    try {
                        selectedElections = rmi.selectElections(name, department);
                        break;
                    } catch (ConnectException e) {
                        //System.out.println("Error");
                    }
                }

                if (selectedElections.size() != 0) {
                    printElections(selectedElections);
                    System.out.println("Choose one of the elections above: ");
                    electionIndex = writeNumber(selectedElections.size());
                    election = selectedElections.get(electionIndex - 1);
                    sendMsg("type | authentication; id | " + terminalId + "; name | " + name + "; password | " + password, multicast_port);
                    System.out.println("Please direct yourself to the terminal in order to vote");
                    while (!sendTrigger.get(terminalId)) {
                        sleep(1000);
                    }
                    sendLists(terminalId, election);
                    sendTrigger.put(Integer.parseInt(parsedInput.get("id")), false);
                    sleep(1000);
                    sendMsg("type | goVote; id | " + terminalId + "; name | " + name + "; election | " + election.getTitle(), multicast_port);
                } else
                    System.out.println("The user cannot vote in any more elections");
            } else
                System.out.println("No terminal available");
        } else
            System.out.println("User not found or Username/Password is incorrect");

        handler_console = new console();
    }

    /**
     * Função que escolhe automaticamente o primeiro terminal que não esteja a ser usado quando um pedido
     * de voto é emitido
     * @return o id do terminal a ser usado ou 0 caso todos estejam a ser ocupados/não haja nenhum ligado
     */
    private static int chooseTerminal() {
        if (terminals.size() == 0) {
            System.out.println("No terminal available\n");
            return 0;
        }
        for (VotingTerminal t: terminals) {
            if (!t.getState()) {
                t.setState(true);
                return t.getID();
            }
        }
        System.out.println("All terminals are being used right now\n");
        return 0;
    }

    /**
     * Função que marca um terminal como estando ocupado/a meio de uso
     * @param terminalId id do terminal a ser dado como ocupado
     */
    private static void occupyTerminal(int terminalId) {
        for (VotingTerminal t: terminals) {
            if (t.getID() == terminalId) {
                t.setState(true);
                return;
            }
        }
        System.out.println("Terminal not connected\n");
    }

    /**
     * A função marca um terminal como estando livre para uso
     * @param terminalId id do terminal a ser marcado como livre
     */
    private static void freeTerminal(int terminalId) {
        for (VotingTerminal t: terminals) {
            if (t.getID() == terminalId) {
                t.setState(false);
                return;
            }
        }
        System.out.println("Terminal not connected\n");
    }

    /**
     * Função feita para imprimir todas as eleições de uma dada ArrayList
     * @param elections ArrayList de eleições a serem imprimidas
     * @throws RemoteException no caso de as funções que usam rmi falharem
     */
    private static void printElections(ArrayList<Election> elections) throws RemoteException {
        for (int i = 0; i < elections.size(); i++) {
            System.out.println((i + 1) + " - " + elections.get(i).getTitle());
        }
    }

    /**
     * Função feita para enviar todas as listas necessárias para o voto para o terminal designado
     * @param terminalId id do terminal que vai usar as mensagens
     * @param election eleição às quais as listas dizem respeito
     * @throws RemoteException no caso de as funções que usam rmi falharem
     */
    private static void sendLists(int terminalId, Election election) throws RemoteException {
        if (election.getLists() == null) {
            System.out.println("Election does not exist or has no candidates\n");
            return;
        }

        for (List l: election.getLists()) {
            sendMsg("type | print; id | " + terminalId + "; listName | " + l.getName(), multicast_port);
        }
    }

    /**
     * Função para obter um número escrito pelo utilizador e garantir que esse número
     * está dentro de um intervalo desejado
     * @param i máximo do intervalo possível do output
     * @return valor do numero introduzido
     * @throws NumberFormatException para garantir que o input é um numero inteiro
     * @throws InputMismatchException para garantir que o número introduzido é um inteiro
     */
    private static int writeNumber(long i) {
        int input = 0;
        boolean exit = false;

        while (!exit) {
            try {
                input = Integer.parseInt(in.nextLine());
                if (input < 1 || input > i)
                    System.out.println("Insert valid number\n");
                else
                    exit = true;
            } catch (NumberFormatException | InputMismatchException e) {
                System.out.println("Insert valid option format\n");
            }
        }

        return input;
    }

    /**
     * Método para enviar mensagem ao MulticastClient
     * @param text text to be sent
     * @param port Multicast port used
     */
    static void sendMsg(String text, int port) {
        MulticastSocket socket = null;

        try {
            socket = new MulticastSocket();
            byte[] buffer = text.getBytes();

            InetAddress group = InetAddress.getByName(INET_ADDR);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            socket.send(packet);
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    /**
     * Thread feita para notificar o servidor RMI constantemente do facto
     * de a mesa de voto estar ativa, assim como do estado dos seus terminais
     */
    static class notifyVTState extends Thread {
        Thread t;

        notifyVTState() throws RemoteException {
            t = new Thread(this);
            t.start();
        }

        public void run() {
            while (true) {
                try {
                    rmi.updateVotingTableState(me, terminals);
                } catch (RemoteException e) {
                    //e.printStackTrace();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
        }
    }

    /**
     * Thread que verifica de 2 em 2 segundos se existe um servidor rmi a correr e caso
     * não tenta ligar-se novamente a um que esteja a correr, neste caso pode-se ligar
     * ao servidor backup de rmi
     */
    static class checkServer extends Thread {
        String message;

        checkServer() {
            this.start();
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(2000);
                    message = rmi.isAlive();
                } catch (RemoteException e) {
                    if(readPropertiesFile()) {
                        try {
                            rmi = (RmiServer) LocateRegistry.getRegistry(host, rmi_port).lookup(rmi_name);
                        } catch (NotBoundException | RemoteException ex) {
                            //ex.printStackTrace();
                        }
                    } //else
                        //System.out.println("Invalid Properties File");
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }
    }
}
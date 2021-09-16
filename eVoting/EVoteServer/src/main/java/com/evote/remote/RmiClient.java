package com.evote.remote;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.text.ParseException;
import java.time.LocalTime;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;

import com.evote.models.Election;
import com.evote.models.User;
import com.evote.models.Vote;
import com.evote.models.VotingTable;
import com.evote.models.VotingTerminal;
import com.evote.models.List;

import java.io.IOException;

/**
 * O RmiClient é a interface Admin Console, tem um menu inicial onde permite
 * fazer todas as opereções disponíveis. Pode haver mais que um admin.
 * O cliente localiza o registo do servidor RMI a partir do porto e do nome.
 */
public class RmiClient {
    static RmiServer rmi;
    private static String host;
    private static String rmi_name;
    private static int rmi_port;
    static Scanner in = new Scanner(System.in);

    private RmiClient() {
        super();
    }

    public static void main(String[] args) throws IOException {
        RmiClient rmiClient = new RmiClient();
        boolean exit = false;

        while (!exit) {
            if (readPropertiesFile()) {
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
        rmiClient.adminConsole();
    }

    /**
     * Função para ler as propriedades do RMI, como o host, o nome e o porto
     * @return true caso tenha lido todos os argumentos esperados, false caso contrário
     */
    public static Boolean readPropertiesFile(){
        boolean h = false, rn = false, rp = false;

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
            }
            reader.close();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        }

        return h && rn && rp;
    }

    /**
     * Função para obter um número escrito pelo utilizador e garantir que esse número
     * está dentro de um intervalo desejado
     * @param i máximo do intervalo possível do output
     * @param acceptZero true se aceitar input zero, false caso contário
     * @return valor do numero introduzido
     * @throws NumberFormatException para garantir que o input é um numero inteiro
     * @throws InputMismatchException para garantir que o número introduzido é um inteiro
     */
    public static long writeNumber(long i, boolean acceptZero) {
        long input = 0;
        boolean exit = false;

        while (!exit) {
            try {
                input = Long.parseLong( in.nextLine());

                if (i == 99999999) {
                    if (input < 10000000 || input > i)
                        System.out.println("Insert valid CC number");
                    else
                        exit = true;
                } else if (i == 999999999) {
                    if (input < 900000000 || input > i)
                        System.out.println("Insert valid phone number");
                    else
                        exit = true;
                } else if (input < 0 || input > i) {
                    if (!acceptZero)
                        System.out.println("Insert valid number");
                } else
                    exit = true;
            } catch (NumberFormatException | InputMismatchException e) {
                System.out.println("Insert valid option format");
            }
        }

        return input;
    }

    /**
     * Função para obter um tipo escrito pelo utilizador
     * @return valor do tipo escolhido
     */
    public static String writeType() {
        String type = null;
        int input = 0;
        boolean exit = false;

        System.out.println("Choose type: 1 - Student | 2 - Teacher | 3 - Employee");
        while (!exit) {
            try {
                input = in.nextInt();
                in.nextLine();

                if (input < 1 || input > 3)
                    System.out.println("Insert option from 1 to 3");
                else
                    exit = true;
            } catch (NumberFormatException | InputMismatchException e) {
                System.out.println("Insert valid option format");
            }
        }

        if (input == 1)
            type = "student";
        if (input == 2)
            type = "teacher";
        if (input == 3)
            type = "employee";

        return type;
    }

    /**
     * Função responsável por receber e interpretar uma data introduzida no formato dia/mês/ano
     * @return data
     */
    public static String writeDate() {
        String date = null;
        boolean exit = false;

        System.out.println("Format dd/MM/yyyy");

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date testDate;

        while (!exit) {
            try {
                date = in.nextLine();
                testDate = df.parse(date);

                if (!df.format(testDate).equals(date)) {
                    System.out.println("Invalid date");
                } else {
                    Date currentDate = Calendar.getInstance().getTime();
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    String strCurrentDate = dateFormat.format(currentDate);
                    if (!compareDates(date, strCurrentDate))
                        System.out.println("Input date has already passed");
                    else
                        exit = true;
                }
            } catch (ParseException e) {
                System.out.println("Invalid format");
            }
        }

        return date;
    }

    /**
     * Função responsável por receber os dados referentes a uma hora e garantir que esta é válida
     * @param startDate dia do início da eleição
     * @param endDate dia do fim da eleição
     * @param startHour hora de início da eleição
     * @return hora
     */
    private String writeHour(String startDate, String endDate, String startHour) {
        int input = 0;
        boolean exit = false;

        while (!exit) {
            try {
                input = in.nextInt();
                in.nextLine();
                if (input < 0 || input > 23)
                    System.out.println("Insert valid hour");
                else {
                    if (startDate.equals(endDate)) {
                        if (input >= LocalTime.now().getHour()) {
                            if (startHour != null) {
                                if (input >= Integer.parseInt(startHour))
                                    exit = true;
                                else
                                    System.out.println("Insert end hour after start hour");
                            } else
                                exit = true;
                        } else
                            System.out.println("Insert hour after current hour");
                    } else
                        exit = true;
                }
            } catch (NumberFormatException | InputMismatchException e) {
                System.out.println("Insert valid option format");
            }
        }
        return String.valueOf(input);
    }

    /**
     * Função responsável por receber os dados referentes a um minuto numa hora e garantir que este é válido
     * @param startDate dia do início da eleição
     * @param endDate dia do fim da eleição
     * @param startHour hora de início da eleição
     * @param endHour hora do fim da eleição
     * @param startMinute minuto da hora do começo da eleição
     * @return minuto
     */
    private String writeMinute(String startDate, String endDate, String startHour, String endHour, String startMinute) {
        int input = 0;
        boolean exit = false;

        while (!exit) {
            try {
                input = in.nextInt();
                in.nextLine();
                if (input < 0 || input > 59)
                    System.out.println("Insert valid minute");
                else {
                    if (startDate.equals(endDate)) {
                        if (endHour != null) {
                            if (startHour.equals(endHour)) {
                                if(input >= LocalTime.now().getMinute()){
                                    if (startMinute != null) {
                                        if (input > Integer.parseInt(startMinute))
                                            exit = true;
                                        else
                                            System.out.println("Insert end minute after start minute");
                                    }
                                } else
                                    exit = true;
                            } else
                                exit = true;
                        } else {
                            if(Integer.parseInt(startHour) == LocalTime.now().getHour()) {
                                if (input >= LocalTime.now().getMinute())
                                    exit = true;
                                else
                                    System.out.println("Insert minute after current minute");
                            } else
                                exit = true;
                        }
                    } else
                        exit = true;
                }
            } catch (NumberFormatException | InputMismatchException e) {
                System.out.println("Insert valid option format");
            }
        }

        return String.valueOf(input);
    }

    /**
     * Compara duas datas e retorna falso de a data 2 for maior que a data 1
     * @param d1 data 1
     * @param d2 data 2
     * @return true se a d1 for depois da d2 ou se forem iguais
     */
    private static boolean compareDates(String d1, String d2) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date date1 = sdf.parse(d1);
            Date date2 = sdf.parse(d2);

            if (date1.after(date2) || date1.equals(date2)) {
                return true;
            }
        } catch (ParseException ex) {
            //ex.printStackTrace();
        }

        return false;
    }

    /**
     * Função que imprime as operações disponíveis da consola do administrador
     * @throws IOException
     */
    public void adminConsole() throws IOException, NumberFormatException {
        int opt;

        System.out.println("\nAdmin Console:");
        System.out.println("1 - Register User");
        System.out.println("2 - Create Election");
        System.out.println("3 - Create List");
        System.out.println("4 - Manage Voting Tables");
        System.out.println("5 - Change Election Properties");
        System.out.println("6 - Get Votes Info");
        System.out.println("7 - Get Voting Tables Status");
        System.out.println("8 - Get Voters in Real Time");
        System.out.println("9 - Get Ended Elections Info");

        opt = (int) writeNumber(10, false);
        chooseAdminMenu(opt);
    }

    /**
     * Função para chamar a função necessária, dependendo da opção escolhida
     * pelo administrados na consola
     * @param opt opção
     * @throws IOException
     */
    public void chooseAdminMenu(int opt) throws IOException {
        switch (opt) {
            case 1:
                registerUser();
                adminConsole();
                break;
            case 2:
                createElection();
                adminConsole();
                break;
            case 3:
                createList();
                adminConsole();
                break;
            case 4:
                manageVotingTables();
                adminConsole();
                break;
            case 5:
                editElection();
                adminConsole();
            case 6:
                getVotesInfo();
                adminConsole();
                break;
            case 7:
                getVotingTablesStates();
                adminConsole();
                break;
            case 8:
                getVotersRealTime();
                adminConsole();
                break;
            case 9:
                getEndedElectionsInfo();
                adminConsole();
                break;
            default:
                System.out.println("Insert valid option");
                break;
        }
    }

    /**
     * Função responsável pela introdução dos dados necessários para a criação de um utilizador
     * @throws RemoteException
     */
    private void registerUser() throws RemoteException {
        String name, role, type, password, department, residenceAddress, ccExpirationDate, reply;
        long phoneNumber, ccNumber;

        System.out.println("Insert name");
        name = in.nextLine();
        /* Modification Start */
        System.out.println("Insert role");
        role = in.nextLine();
        /* Modification End */
        System.out.println("Insert type");
        type = writeType();
        System.out.println("Insert password");
        password = in.nextLine();
        System.out.println("Insert faculty department");
        department = in.nextLine();
        System.out.println("Insert phone number");
        phoneNumber = writeNumber(999999999, false);
        System.out.println("Insert residence address (all in one line)");
        residenceAddress = in.nextLine();
        System.out.println("Insert cc number");
        ccNumber = writeNumber(99999999, false);
        System.out.println("Insert cc expiration date");
        ccExpirationDate = writeDate();

        while (true) {
            try {
                reply = rmi.registerUser(name, role, type, password, department, phoneNumber, residenceAddress, ccNumber, ccExpirationDate);
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        System.out.println(reply);
    }

    /**
     * Função que trata da introdução dos dados para criação de uma eleição
     * @throws RemoteException
     */
    private void createElection() throws RemoteException {
        String title, description, startDate, endDate, startHour, startMinute, endHour, endMinute, type, reply;
        ArrayList<List> lists = new ArrayList <> (), electionLists = new ArrayList <> (), listsTemp;
        int inputUser;

        System.out.println("Insert election title");
        title = in.nextLine();
        System.out.println("Insert election description");
        description = in.nextLine();
        System.out.println("Insert election start date");
        startDate = writeDate();
        System.out.println("Insert election end date");
        endDate = writeDate();
        System.out.println("Insert start hour (format 0-23)");
        startHour = writeHour(startDate, endDate, null);
        System.out.println("Insert start minute (format 0-59)");
        startMinute = writeMinute(startDate, endDate, startHour, null, null);
        System.out.println("Insert end hour (format 0-23)");
        endHour = writeHour(startDate, endDate, startHour);
        System.out.println("Insert end minute (format 0-59)");
        endMinute = writeMinute(startDate, endDate, startHour, endHour, startMinute);
        System.out.println("Insert type");
        type = writeType();

        while (true) {
            try {
                listsTemp = rmi.getLists();
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        for (List l: listsTemp) {
            if (l.getType().equals(type)) {
                lists.add(l);
            }
        }

        System.out.println("Choose a candidate list to add to the election (0 to end):");
        while (true) {
            for (int i = 0; i < lists.size(); i++) {
                System.out.println((i + 1) + " - " + lists.get(i).getName());
            }
            inputUser = (int) writeNumber(lists.size(), true);
            if (inputUser == 0)
                break;
            else
                electionLists.add(lists.get(inputUser - 1)); //was index instead of inputUser
        }

        for (int i = 0; i < lists.size(); i++) {
            System.out.println((i + 1) + " - " + lists.get(i).getName());
        }

        while (true) {
            try {
                reply = rmi.createElection(title, description, startDate, endDate, startHour, startMinute, endHour, endMinute, type, electionLists);
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        System.out.println(reply);
    }

    /**
     * Função responsável pela introdução de dados para se criar uma lista candidata
     * @throws RemoteException
     */
    private void createList() throws RemoteException {
        String name, type, reply;
        ArrayList<User> users;
        ArrayList<Integer> usersIndex = new ArrayList <> ();
        ArrayList<User> listUsers = new ArrayList <> ();
        int index = 1, inputUser;

        System.out.println("Insert list name");
        name = in.nextLine();

        type = writeType();

        while (true) {
            try {
                users = rmi.getUsers();
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        System.out.println("Choose users (write 0 to finish)");
        while (true) {
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getType().equals(type)) {
                    System.out.println(index + " - " + users.get(i).getName());
                    usersIndex.add(i);
                    index++;
                }
            }
            inputUser = (int) writeNumber(users.size(), true);
            index = 1;
            if (inputUser == 0)
                break;
            else
                listUsers.add(users.get(usersIndex.get(inputUser - 1))); //was index instead of inputUser
        }

        while (true) {
            try {
                reply = rmi.createList(name, type, listUsers);
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        System.out.println(reply);
    }

    /**
     * Função responsável pela gerência das mesas de voto, podendo-se adicionar ou remover uma eleição associada a essa mesa de voto
     * @throws RemoteException
     */
    private void manageVotingTables() throws RemoteException {
        int indexTable, indexElectionToRemove, opt;
        String reply;
        CopyOnWriteArrayList<VotingTable> votingTables;
        Election electionToAdd, electionToRemove;

        while (true) {
            try {
                votingTables = rmi.getVotingTables();
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        for (int i = 0; i < votingTables.size(); i++)
            System.out.println((i + 1) + " - " + votingTables.get(i).getDepartment());

        System.out.println("Choose voting table");
        indexTable = ((int) writeNumber(votingTables.size(), false)) - 1;

        System.out.println("Choose option: 1 - Add election | 2 - Remove election");
        opt = (int) writeNumber(2, false);

        if (opt == 1) {
            electionToAdd = chooseElection();

            while (true) {
                try {
                    reply = rmi.manageVotingTables(votingTables.get(indexTable), electionToAdd, "add");
                    break;
                } catch (ConnectException e) {
                    //System.out.println("Error");
                }
            }
            System.out.println(reply);
        }
        if (opt == 2) {
            System.out.println(indexTable);
            for (int i = 0; i < votingTables.get(indexTable).getElections().size(); i++)
                System.out.println((i + 1) + " - " + votingTables.get(indexTable).getElections().get(i).getTitle());

            System.out.println("Choose election to remove");
            indexElectionToRemove = (int) writeNumber(votingTables.get(indexTable).getElections().size(), false);
            electionToRemove = votingTables.get(indexTable).getElections().get(indexElectionToRemove - 1);
            votingTables.get(indexTable).removeElection(electionToRemove);
        }
    }

    /**
     * Função para escolher uma eleição
     * @return a eleição escolhida
     * @throws RemoteException
     */
    private Election chooseElection() throws RemoteException {
        int index;
        ArrayList<Election> elections;

        while (true) {
            try {
                elections = rmi.getElections();
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        index = 1;
        for (Election e: elections){
            System.out.println(index + " - " + e.getTitle());
            index++;
        }

        System.out.println("Choose election");
        index = (int) writeNumber(elections.size(), false);

        return elections.get(index - 1);

    }

    /**
     * Função responsável pela edição de detalhes de uma eleição
     * @throws RemoteException
     */
    private void editElection() throws RemoteException {
        String startDate, endDate, newValue = null, reply;
        int toEdit;
        boolean exit = false;
        Election election;
        ArrayList<Election> elections;

        while (true) {
            try {
                elections = rmi.getElections();
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        election = chooseElection();
        startDate = election.getStartDate();
        endDate = election.getEndDate();

        Date currentDate = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String strCurrentDate = dateFormat.format(currentDate);

        //Just if election start date> current date
        if (!compareDates(startDate, strCurrentDate)) {
            System.out.println("Election already started!");
            return;
        }

        System.out.println("Choose option to edit: 1 - Title | 2 - Description | 3 - Start Date | 4 - End Date | 5 - Start Hour | 6 - Start Minute | 7 - End Hour | 8 - End Minute | 9 - Type");
        toEdit = (int) writeNumber(7, false);

        if (toEdit == 1) {
            boolean flag = false;
            while (!exit) {
                System.out.println("Write new title");
                newValue = in.nextLine();

                for (Election e: elections) {
                    if (e.getTitle().equals(newValue)) {
                        System.out.println("Title is already taken");
                        flag = true;
                        break;
                    }
                }
                exit = !flag;
            }
        }

        exit = false;

        if (toEdit == 2) {
            System.out.println("Write new description");
            newValue = in.nextLine();
        }

        if (toEdit == 2) {
            while (!exit) {
                System.out.println("Write new title");
                newValue = in.nextLine();

                if (!compareDates(startDate, strCurrentDate))
                    System.out.println("Start date must be greater than current date!");
                else if (compareDates(startDate, endDate))
                    System.out.println("Start date must be smaller than end date!");
                else
                    exit = true;
            }
        }

        exit = false;

        if (toEdit == 3) {
            while (!exit) {
                System.out.println("Write new start date");
                newValue = writeDate();

                //Start date > Current date && Start date < End date
                if (!compareDates(startDate, strCurrentDate))
                    System.out.println("Start date must be greater than current date!");
                else if (compareDates(startDate, endDate))
                    System.out.println("Start date must be smaller than end date!");
                else
                    exit = true;
            }
        }

        exit = false;

        if (toEdit == 4) {
            while (!exit) {
                System.out.println("Write new end date");
                newValue = writeDate();

                //End date > Current date && End date > Start date
                if (compareDates(strCurrentDate, endDate))
                    System.out.println("End date must be greater than current date!");
                else if (!compareDates(startDate, endDate))
                    System.out.println("End date must be greater than start date!");
                else
                    exit = true;
            }
        }

        if (toEdit == 5)
            newValue = writeHour(startDate, endDate, election.getStartHour());

        if (toEdit == 6)
            newValue = writeMinute(startDate, endDate, election.getStartHour(), election.getEndHour(), election.startMinute);

        if (toEdit == 7)
            newValue = writeHour(startDate, endDate, election.getStartHour());

        if (toEdit == 8)
            newValue = writeMinute(startDate, endDate, election.getStartHour(), election.getEndHour(), election.startMinute);

        if (toEdit == 9)
            newValue = writeType();

        while (true) {
            try {
                reply = rmi.editElection(election, toEdit, newValue);
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        System.out.println(reply);
    }

    /**
     * Função responsável por demonstrar os votos registados e os seua detalhes respetivos
     * @throws RemoteException
     */
    private void getVotesInfo() throws RemoteException {
        ArrayList<Vote> votes;

        while (true) {
            try {
                votes = rmi.getVotes();
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        System.out.println("Normal votes");
        for (Vote v: votes) {
            if(!v.whiteVote && !v.nullVote) {
                System.out.println("Department: " + v.getDepartment());
                System.out.println("Moment: " + v.getMoment());
            }
        }

        System.out.println("\nWhite votes");
        for (Vote v: votes) {
            if(v.whiteVote) {
                System.out.println("Department: " + v.getDepartment());
                System.out.println("Moment: " + v.getMoment());
            }
        }

        System.out.println("\nNull votes");
        for (Vote v: votes) {
            if(v.nullVote) {
                System.out.println("Department: " + v.getDepartment());
                System.out.println("Moment: " + v.getMoment());
            }
        }
    }

    /**
     * Função responsável por demonstrar todos os votos até ao momento
     * @throws IOException
     */
    private void getVotersRealTime() throws IOException {
        ArrayList<Vote> votes, currentVotes;
        while (true) {
            try {
                votes = rmi.getVotes();
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        ArrayList<String> elections = new ArrayList<>();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int count;
        boolean begin = true;

        System.out.println("Click any key to exit");

        while (!in.ready()) {
            while (true) {
                try {
                    currentVotes = rmi.getVotes();
                    break;
                } catch (ConnectException e) {
                    //System.out.println("Error");
                }
            }

            if (votes.size() != currentVotes.size() || begin) {

                votes = currentVotes;

                for (Vote v: votes) {
                    if(v.getElection() != null)
                        elections.add(v.getElection().getTitle());
                }

                Set<String> distinct = new HashSet<>(elections);
                for (String e : distinct) {
                    count = 0;
                    for (Vote v : votes) {
                        if (v.getElection().getTitle().equals(e))
                            count++;
                    }
                    System.out.println(e + ": " + count);
                }

                begin = false;
            }
        }
    }

    /**
     * Função responsável por demonstrar as eleições que já terminaram e os seus dados, incluindo os seus votos
     * @throws RemoteException
     */
    private void getEndedElectionsInfo() throws RemoteException {
        ArrayList<Election> elections;
        ArrayList<Vote> votes, electionVotes = new ArrayList<>();
        ArrayList<String> electionVotesLists = new ArrayList<>();
        int numberOfVotes, numberOfWhiteVotes = 0;
        float percentage;

        while (true) {
            try {
                elections = rmi.getElections();
                votes = rmi.getVotes();
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        for (Election e: elections) {
            if(!e.getState()) {
                System.out.print("Title: " + e.getTitle());

                for(Vote v: votes){
                    if(v.election.equals(e)) {
                        electionVotes.add(v);
                        electionVotesLists.add(v.getList().getName());
                        if(v.whiteVote)
                            numberOfWhiteVotes++;
                    }
                }

                if(electionVotes.size() == 0){
                    System.out.print(" - No votes available\n");
                } else {
                    Set<String> distinct = new HashSet<>(electionVotesLists);
                    for (String s : distinct) {
                        numberOfVotes = (Collections.frequency(electionVotesLists, s));
                        percentage = electionVotesLists.size() / numberOfVotes;
                        System.out.println(s + ": " + percentage + "%\n");
                    }
                    percentage = electionVotesLists.size() / numberOfWhiteVotes;
                    System.out.println("White votes: " + "count - " + numberOfWhiteVotes + " percentage - " + percentage);
                }
            }
        }
    }

    /**
     * Função responsável por demonstrar os estados e informações das mesas de votos
     * @throws RemoteException
     */
    private void getVotingTablesStates() throws RemoteException {
        CopyOnWriteArrayList<VotingTable> votingTables;
        String state;

        while (true) {
            try {
                votingTables = rmi.getVotingTables();
                break;
            } catch (ConnectException e) {
                //System.out.println("Error");
            }
        }

        for (VotingTable vt: votingTables) {
            System.out.println("Voting Table");
            System.out.println("Department: " + vt.getDepartment());
            if (vt.getState())
                state = "active";
            else
                state = "inactive";
            System.out.println("State: " + state);
            System.out.println("Terminals");
            for (VotingTerminal t: vt.votingTerminals) {
                System.out.println("ID: " + t.getID());
                if (t.getState())
                    state = "active";
                else
                    state = "inactive";
                System.out.println("State: " + state);
            }
        }
    }

    /**
     * Thread que verifica de 2 em 2 segundos se existe um servidor rmi a correr e
     * caso não tenta ligar-se novamente a um que esteja a correr, neste caso pode-se
     * ligar ao servidor backup de rmi
     *
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
package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
private final int PORT = 8189;
private Vector<ClientHandler> clients; // Vektor это ArrayList с методами Synchronized - потокобезопасный.
    private AuthService authService;
    public AuthService getAuthService() {return authService;}


    public Server() {
        ServerSocket server = null;
        Socket socket = null;
        authService = null;
        clients = new Vector<>();
        try {
            server = new ServerSocket(PORT); // создание нового сервера который слушает порт (8189)
            authService = new BaseAuthService();
            authService.start(); // Пока что не делает ничего - далее запускает проверку с Базой Данных
            System.out.println("Сервер запущен ждем клиентов");

            while (true) {
                socket = server.accept(); //Серверный СОКЕТ принимает клиентские СОКЕТЫ режим ожидания ,
                //clients.add(new ClientHandler(socket,this));
                subscribeMe(new ClientHandler(socket, this));
                System.out.println("Клиент подключился");
            }
        } catch (
                IOException e) {  // Обработка ошибок с сетью
            e.printStackTrace();
            System.out.println("Не удалось запустить сервер");
        } finally {
            try {
                socket.close();
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            authService.stop();
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nick)) return true;
        }
        return false;
    }

    public void broadcast(String msg){  // Транслятор сообщений всем чатам
        for (ClientHandler c: clients){
            c.sendMessage(msg);
        }
    }

    public void broadcastUsrList(){
        StringBuffer sb = new StringBuffer("/userslist");
        for (ClientHandler c: clients){
            sb.append(" " + c.getName());
        }
        for (ClientHandler c: clients){
            c.sendMessage(sb.toString());
        }
    }

    public synchronized void sendMessageTo(ClientHandler from, String to, String msg){
        for(ClientHandler c: clients){
            if(c.getName().equalsIgnoreCase(to)){
                c.sendMessage("from " + from.getName() + ": " + msg);
                from.sendMessage("to " + to + " msg " + msg);
                break;
            }
        }
    }

    public void subscribeMe(ClientHandler c){
        clients.add(c);
        broadcastUsrList();
    }

    public void unsubscribeMe(ClientHandler c){
        clients.remove(c);
        broadcastUsrList();
    }


}
package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler { // Обработчик клиента (Отвечает за обмен сообщениями между клиентом и сервером)
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    //public String getName() {
   //     return name;
   // }

    public ClientHandler(Socket socket, Server server) {
        try {
            this.server = server;
            this.socket = socket;
            name = "";
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            // this.name = "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            try {
                while (true) { // цикл авторизации
                    //String str = in.readUTF();
                    String str = null;
                    try {
                        str = in.readUTF();
                        if (str.startsWith("/auth")) {
                            String[] parts = str.split(" ");
                            String nick = server.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                            if (nick != null) {
                                if (!server.isNickBusy(nick)) {
                                    sendMessage("/authok " + nick);
                                    this.name = nick;
                                    server.broadcast(this.name + " зашел в чат");
                                    server.broadcastUsrList();
                                    //server.subscribeMe(this);
                                    break;
                                } else sendMessage("Учетная запись уже используется");
                            } else sendMessage("Неверные логин/пароль");
                        } else sendMessage("для начала нужно авторизоваться");
                    } catch (EOFException e) {
                    }
                } // Конец Авторизации

                while (true) { // цикл получения сообщений
                    String str = null;
                    try {
                        str = in.readUTF();
                        //System.out.println("от " + name + ": " + str);
                        if (str.equalsIgnoreCase("/end")) break;
                        if (str.startsWith("/w")) { //w nick2 Hello, how are you? Личные сообщения.
                            String[] string = str.split(" ", 3);
                            String nameTo = string[1];
//                            String message = str.substring(4 + nameTo.length());
                            String message = string[2];
                            server.sendMessageTo(this, nameTo, message);
                        } else {
                            server.broadcast(this.name + " " + str);
                        }
                        System.out.println("client: " + str);
                        //                        sendMessage("echo: " + str);

                    } catch (EOFException e) { }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                server.unsubscribeMe(this);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void sendMessage(String msg){
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName(){
        return name;
    }
}

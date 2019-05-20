package Client;

import com.sun.xml.internal.ws.wsdl.writer.document.PortType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatWindow extends JFrame {
    private JTextArea jTextArea;
    private JTextField jTextField;
    private JPanel bottomPanel, upperPanel;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean isAuthorized;
    private JTextArea jtaUsers;
    private JScrollPane jspUsers;

    public void setAuthorized(boolean authorized){
        isAuthorized = authorized;
        upperPanel.setVisible(!isAuthorized);
        bottomPanel.setVisible(isAuthorized);
        jspUsers.setVisible(isAuthorized);
    }

    public ChatWindow(){
        setTitle("Chat Window");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(400,300, 400,400);
        jTextArea = new JTextArea();
        jTextField = new JTextField();
        jtaUsers = new JTextArea();
        jtaUsers.setPreferredSize(new Dimension(150,1));
        jtaUsers.setEditable(false);
        jspUsers = new JScrollPane(jtaUsers);
        JScrollPane jsp = new JScrollPane(jTextArea);
        jTextField.setPreferredSize(new Dimension(200,20));
        jTextArea.setEditable(false);
        jTextArea.setLineWrap(true);
        JButton jButtonSend = new JButton("Send");

        bottomPanel = new JPanel();
        bottomPanel.add(jTextField, BorderLayout.CENTER);
        bottomPanel.add(jButtonSend, BorderLayout.EAST);
        upperPanel = new JPanel(new GridLayout(1,3));
        JTextField jtfLogin = new JTextField();
        JPasswordField jtfPass = new JPasswordField();
        JButton jbAuth = new JButton("Login");

        jbAuth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                try{
                    out.writeUTF("/auth" + " " + jtfLogin.getText() + " " + jtfPass.getText());
                }catch(IOException e1){
                    e1.printStackTrace();
                }
            }
        });

        upperPanel.add(jtfLogin);
        upperPanel.add(jtfPass);
        upperPanel.add(jbAuth);

        add(upperPanel, BorderLayout.NORTH);
        add(jsp, BorderLayout.CENTER);
        add(jspUsers, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        jButtonSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                sendMessage();
            }
        });
        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                sendMessage();
            }
        });
        try{
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        }catch(IOException e){
            e.printStackTrace();
            setAuthorized(false);
        }
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run(){
                try{
                    while(true){
                        String msg = in.readUTF();
                        if(msg.startsWith("/authok")){
                            setAuthorized(true);
                            break;
                        }else if(!msg.startsWith("/userslist")){
                            jTextArea.append(msg + "\n");
                            jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
                        }
                    }
                    while(true){
                        String msg = in.readUTF();
                        if(msg.startsWith("/")){
                            if(msg.startsWith("/userslist")){
                                String[] users = msg.split(" ");
                                jtaUsers.setText("");
                                for(int i = 1; i < users.length; i++){
                                    jtaUsers.append(users[i] + "\n");
                                }
                            }
                        }else{
                            jTextArea.append(msg + "\n");
                            jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
                        }
                    }
                }catch(IOException e){
                    e.printStackTrace();
                    setAuthorized(false);
                }finally{
                    try{
                        socket.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread1.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                super.windowClosing(e);
                try{
                    out.writeUTF("/end");
                    socket.close();
                }catch(IOException e1){
                    e1.printStackTrace();
                    setAuthorized(false);
                }
            }
        });
        setAuthorized(false);
        setVisible(true);
    }
    public void sendMessage(){
        String msg = jTextField.getText();
        jTextField.setText("");
        try{
            out.writeUTF(msg);
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
package ru.mikhailr.emulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Вначале запускается Лифт, затем Клиент.
 * Приложение клиента для взаимодействия с лифтом.
 * Клиент общается с лифтом посредством консоли.
 * Соединение между клиентом и сервером устанавливается с помощью сокетов.
 * Клиент может отправить Лифту этажи на которые надо ехать, отправлять можно несколько этажей сразу
 * Можно отправлять новые этажи, в момент движения лифта. Они встанут в очередь.
 * Любая отправка сообщения Лифту в ответ сообщает состояние лифта.
 * Для того чтобы просто узнать состояние, не вызывая лифт на этажи, достаточно отправить
 * любой Нечисловой символ.
 * @author Mikhail Rozdin
 * @version $Id$
 * @since 0.1
 */
public class Client {
    /**
     * Основной сокет.
     */
    private final Socket socket;


    public Client(Socket mainSocket) {
        this.socket = mainSocket;

    }

    /**
     * Метод запуска Клиента. К моменту запуска данного метода Лифт должен быть уже запущен.
     * Для завершения работы отправить лифту "exit"
     */
    public void start() {
        try (PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()))) {
            Scanner console = new Scanner(System.in);
            String tmp = in.readLine();
            String query;
            while (!tmp.equals("bye")) {
                System.out.println(tmp);
                query = console.nextLine();
                out.println(query);
                tmp = in.readLine();
            }
            System.out.println(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        Socket socket =  new Socket("127.0.0.1", 5555);
        Client client = new Client(socket);
        client.start();
    }
}
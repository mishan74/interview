package ru.mikhailr.emulator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Эмулятор движения лифта.
 * Принимает сообщения до тех пор, пока не получит сообщение о выходе
 * "exit".
 * Может передвигаться от первого до указанного этажей.
 * Преодалевает расстояние 1 этаж за 10 секунд.
 * Остановка и старт лифта по 2 секунды.
 * Процесс движения лифта происходит в отдельном потоке, поэтому
 * лифт может во время движения принимать сообщения от Клиента.
 * @author Mikhail Rozdin
 * @version $Id$
 * @since 0.1
 */
public class Elevator {
    private static final String STARTING = "starting move";
    private static final String FINISHED = "finished move";
    /**
     * Основной сокет
     */
    private final Socket socket;
    /**
     * Максимальное количество этажей
     */
    private volatile int stages;
    /**
     * Очередь движения по этажам.
     */
    private final Queue<Integer> way = new PriorityBlockingQueue<>();
    private  int position = 1;
    private final ActionDelay action = new ActionDelay();
    private String state = "The elevator wait on the 1st floor";


    public Elevator(Socket socket, int stages) {
        this.socket = socket;
        this.stages = stages;
    }

    /**
     * Метод заполнения очереди движения лифта.
     * Принимает входящее сообщение от клиента, если оно
     * содержит число, от 1 до указанных этажей,
     * число попадает в очередь движения лифта.
     * @param message входящее сообщение от Клиента.
     */
    private void fillWay(String message) {
        Scanner tempFloors = new Scanner(message);
        while (tempFloors.hasNextInt()) {
            way.add(tempFloors.nextInt());
        }
        tempFloors.close();
    }

    /**
     * Методы установки состояния лифта.
     * @param state Состояние лифта.
     * @param to Этаж.
     */
    private void setState(String state, int to) {
        this.state = state + " " + to + " floor";
    }
    private void setState(int from, int to) {
        this.state = "The elevator move from " + from + " floor to "
                + to + " wright going to the " + position + " floor";
    }

    private String getState() {
        return this.state;
    }

    /**
     * Метод эмуляции движения лифта.
     * В качестве параметра принимает целевой этаж.
     * Вначале и в конце движения задержка по 2 секунды
     * каждый этаж преодолевается за 10 секунд.
     * @param dest этаж до которого двигаться.
     */
    private void startMove(int dest) {
        int start = position;

        action.startBefore(2000, ()->setState(STARTING, dest));
        while (dest != position) {
            action.startBefore(10000, ()-> {
                if (dest > start) {
                    position++;
                } else {
                    position--;
                }
                setState(start, dest);
            });
        }
        action.startAfter(2000, ()->setState(FINISHED, dest));
    }

    /**
     * Циклический метод работы лифта.
     * Принимает сообщения до тех пор, пока не получит сообщение о выходе
     * "exit".
     */
    public void start() {

        elevatorMove.setDaemon(true);
        elevatorMove.start();
        try (PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()))) {
            String ask = "";
            out.println(getState());
            while (!("exit".equals(ask))) {
                ask = in.readLine();
                System.out.println(ask);
                if (!ask.equals("exit")) {
                    fillWay(ask);
                    out.println(getState());
                }
            }
            out.println("bye");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Поток, вызывающий метод движения лифта.
     */
    final Thread elevatorMove = new Thread(()-> {
        while (true) {
            if (!way.isEmpty()) {
                int temp = way.poll();
                if (temp != position && temp <= stages && temp >= 1) {
                    startMove(temp);
                }
            }
        }
    });

    public static void main(String[] args) throws IOException {

        ServerSocket mainSocket = new ServerSocket(5555);
        Elevator elevator = new Elevator(mainSocket.accept(), 7);
        elevator.start();
    }
}
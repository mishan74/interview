package ru.mikhailr.emulator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Эмулятор движения лифта.
 * Принимает сообщения до тех пор, пока не получит сообщение о выходе
 * "exit".
 * Может передвигаться от первого до указанного этажей.
 * Преодалевает расстояние 1 этаж за 10 секунд.
 * Остановка и старт лифта по 2 секунды.
 * Лифт может во время движения принимать сообщения от Клиента.
 * Движение лифта контролируется объектом ElevatorActions.
 *
 * @author Mikhail Rozdin
 * @version $Id$
 * @since 0.1
 */
public class Elevator {
    /**
     * Основной сокет
     */
    private final Socket socket;
    /**
     * Максимальное количество этажей
     */
    private final int stages;
    /**
     * Очередь движения по этажам.
     */
    private final Queue<Integer> way = new LinkedList<>();
    private final ElevatorActions elevatorActions = new ElevatorActions(this);

    private int position = 1;

    private String status;

    public Elevator(Socket socket, int stages) {
        this.socket = socket;
        this.stages = stages;
    }

    public Queue<Integer> getWay() {
        return this.way;
    }

    public int getPosition() {
        return this.position;
    }

    public void incrementPosition() {
        this.position++;
    }

    public void decrementPosition() {
        this.position--;
    }

    public int getStages() {
        return this.stages;
    }

    /**
     * Метод установки состояния лифта.
     * @param status Состояние лифта.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Метод заполнения очереди движения лифта.
     * Принимает входящее сообщение от клиента, если оно
     * содержит число, от 1 до указанных этажей,
     * число попадает в очередь движения лифта.
     *
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
     * Циклический метод работы лифта.
     * Принимает сообщения до тех пор, пока не получит сообщение о выходе
     * "exit".
     */
    public void start() {
        try (PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()))) {
            String ask = "";
            elevatorActions.exec();
            out.println(this.status);
            while (!("exit".equals(ask))) {
                ask = in.readLine();
                System.out.println(ask);
                if (!ask.equals("exit")) {
                    fillWay(ask);
                    elevatorActions.exec();
                    out.println(this.status);
                }
            }
            out.println("bye");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        ServerSocket mainSocket = new ServerSocket(5555);
        Elevator elevator = new Elevator(mainSocket.accept(), 7);
        elevator.start();
    }
}
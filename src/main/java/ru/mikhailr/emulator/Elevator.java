package ru.mikhailr.emulator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Программа для получения вероятного состояния движения лифта.
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
    private final Map<Date, Integer> way = new LinkedHashMap<>();
    private final int pause = 2;
    private final int speed = 5;
    private int lastAddedStage = 1;
    private Date lastAddedTime;
    private final ElevatorActions elevatorActions = new ElevatorActions(this.way, this);

    public Elevator(Socket socket, int stages) {
        this.socket = socket;
        this.stages = stages;
    }

    public int getPause() {
        return this.pause;
    }

    public int getSpeed() {
        return this.speed;
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
            int temp = tempFloors.nextInt();
            Calendar calendar = Calendar.getInstance();
            if (temp >= 1 && temp <= stages) {
                if (temp != lastAddedStage) {
                    if (!way.isEmpty()) {
                        calendar.setTime(lastAddedTime);
                    }
                    calendar.add(Calendar.SECOND, 2 * pause + (speed * Math.abs(temp - lastAddedStage)));
                    way.put(calendar.getTime(), temp);
                    lastAddedTime = calendar.getTime();
                    lastAddedStage = temp;
                }
            }
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
            elevatorActions.cleanMap();
            out.println(elevatorActions.exec());
            while (!("exit".equals(ask))) {
                ask = in.readLine();
                System.out.println(ask);
                if (!ask.equals("exit")) {
                    elevatorActions.cleanMap();
                    fillWay(ask);
                    out.println(elevatorActions.exec());
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
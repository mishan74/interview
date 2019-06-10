package ru.mikhailr.emulator;

import java.util.*;

/**
 * @author Mikhail Rozdin
 * @version $Id$
 * @since 0.1
 */

/**
 * Контролирует движение лифта.
 */
public class ElevatorActions {

    private Date lastFinishTime = Calendar.getInstance().getTime();
    private int lastFinishStage = 1;
    private final int pause;
    private final int speed;


    private final Map<Date, Integer> way;

    /**
     * Сообщение от состояния лифта.
     */
    private String condition;

    private Status status = Status.WAIT;

    /**
     * Возможные состояния лифта.
     */
    private enum Status {
        WAIT, STARTING, RUNNING, FINISHING
    }

    public ElevatorActions(Map<Date, Integer> way, Elevator elevator) {
        this.way = way;
        this.pause = elevator.getPause();
        this.speed = elevator.getSpeed();
    }

    /**
     * Основной метод, возвращает сообщение от состояния лифта.
     * @return condition.
     */
    public String exec() {
        initStatus(getDateFromMap(), Math.abs(getStageFromMap() - lastFinishStage));
        getToSwitch(lastFinishStage, getStageFromMap());
        return this.condition;
    }

    /**
     * Метод очистки карты отображений от пройденных лифтов.
     */
    public void cleanMap() {
        Date temp = new Date();
        Iterator<Date> itr = way.keySet().iterator();
        while (itr.hasNext()) {
            Date date = itr.next();
            if (date.after(temp)) {
                break;
            }
            lastFinishTime = date;
            lastFinishStage = way.get(lastFinishTime);
            itr.remove();
        }
    }

    private Date getDateFromMap() {

        return !way.isEmpty() ? way.keySet().iterator().next() : lastFinishTime;

    }

    private int getStageFromMap() {

        return !way.isEmpty() ? way.values().iterator().next() : 0;

    }

    /**
     * Проверка, в каком сейчас состоянии находится лифт.
     * Проверяется по дате из карты отображений либо по дате последнего прибытия, если карта пуста.
     * @param date Дата.
     * @param floor Количество этажей, которые необходимо пройти.
     */
    private void initStatus(Date date, int floor) {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, -pause);
        Date finish = calendar.getTime();
        calendar.add(Calendar.SECOND, -speed * floor);
        Date run = calendar.getTime();
        calendar.add(Calendar.SECOND, -pause);
        Date start = calendar.getTime();

        if (now.after(date)) {
            this.status = Status.WAIT;
        } else if (now.after(finish)) {
            this.status = Status.FINISHING;
        } else if (now.after(run)) {
            this.status = Status.RUNNING;
        } else if (now.after(start)) {
            this.status = Status.STARTING;
        }
    }

    /**
     * Метод генерирует сообщение исходя из текущего состояния.
     * @param position стартовый этаж.
     * @param dest этаж назначения.
     */
    private void getToSwitch(int position, int dest) {
            switch (status) {
            case WAIT:
                condition = String.format("Wait on the %d floor", position);
                break;

            case STARTING:
                condition = String.format("Starting from the %d to the %d floor", position, dest);
                break;

            case RUNNING:
                condition = String.format("Running from the %d to the %d floor",
                        position, dest);
                break;

            case FINISHING:
                condition = String.format("Finished to the %d floor", dest);
                break;

            default:
                throw new UnsupportedOperationException("Неизвестное состояние");

        }
    }
}

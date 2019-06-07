package ru.mikhailr.emulator;


import javax.swing.*;

/**
 * Из класса вызываются действия состояний.
 * Некоторые состояния вызывают таймер с отложенным действием.
 * Для каждого состояния ининиализирован свой таймер(если таймер необходим).
 *
 * Какие на первый взгляд проблемы:
 * Зачем приватные сет методы? - на случай если появятся специфичные условия для сета.
 * Почему не определить поле позиции лифта, не обращаясь каждый раз к объекту лифта? - на случай если позиция лифта
 * изменится вне данного класса.
 *
 *
 * @author Mikhail Rozdin
 * @version $Id$
 * @since 0.1
 */
public class ElevatorActions {
    private Elevator el;
    private Timer timer;
    private State state;
    private int dest;

    /**
     * В качестве аргумента объект лифта, через публичные методы которого происходит управление.
     * @param elevator elevator.
     */
    ElevatorActions(Elevator elevator) {
        this.el = elevator;
    }

    /**
     * изначально метод
     * вызывает действие из первого состояния.
     * Когда поле State state станет проинициализированно каким либо состоянием (в данном случае первым же),
     * то тогда в дальнейшем будет вызываться метод поля State state
     */
    public void exec() {
        if (state == null) {
            waitingToWay.doThis();
        }
        state.doThis();
    }

    private void setState(State state) {
        this.state = state;
    }

    private void setTimer(Timer timer) {
        this.timer = timer;
    }

    /**
     * Сосотояние ожидания движения.
     */
    private final State waitingToWay = new State() {
        @Override
        public void doThis() {
            if (state != this) {
                setState(this);
                el.setStatus("Лифт ожидает движение на " + el.getPosition() + " этаже");
            }
            startState.doThis();
        }
    };

    /**
     * Состояние начала движения.
     */
    private final State startState = new State() {
        @Override
        public void doThis() {
            if (state != this) {
                if (!el.getWay().isEmpty()) {
                    dest = el.getWay().poll();
                    if (dest == el.getPosition() || dest < 1 || dest > el.getStages()) {
                        this.doThis();
                    } else {
                        setState(this);
                        startTimer.setRepeats(false);
                        setTimer(startTimer);
                        timer.start();
                        el.setStatus("Лифт начал движение в сторону " + dest + " этажа");
                    }
                }
            }
        }
    };

    /**
     * Состояние процесса движения.
     */
    private final State moveState = new State() {
        @Override
        public void doThis() {
            if (state != this) {
                setState(this);
                timer = moveTimer;
                timer.start();
                el.setStatus("Лифт сейчас на " + el.getPosition() + " этаже, движется в сторону " + dest + " этажа");
            }
        }
    };

    /**
     * Состояние окончания движения.
     */
    private final State finishState = new State() {
        @Override
        public void doThis() {
            if (state != this) {
                setState(this);
                timer = finishTimer;
                timer.start();
                el.setStatus("Лифт заканчивает ехать на " + dest + " этаж");
            }
        }
    };

    /**
     * Таймер для начала движения.
     */
    private final Timer startTimer = new Timer(2000, e -> moveState.doThis());

    /**
     * Таймер для процесса движения.
     */
    private final Timer moveTimer = new Timer(5000, e -> {
        if (dest > el.getPosition()) {
            el.incrementPosition();
        } else if (dest < el.getPosition()) {
            el.decrementPosition();
        }
        el.setStatus("Лифт сейчас на " + el.getPosition() + " этаже, движется в сторону " + dest + " этажа");
        if (el.getPosition() == dest) {
            timer.stop();
            finishState.doThis();
        }
    });

    /**
     * Таймер для окончания движения.
     */
    private final Timer finishTimer = new Timer(2000, e -> {
        el.setStatus("Лифт приехал на " + dest + " этаж");
        timer.stop();
        waitingToWay.doThis();
    });
}

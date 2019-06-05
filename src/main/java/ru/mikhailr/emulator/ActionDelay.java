package ru.mikhailr.emulator;

import java.util.Calendar;

/**
 * Класс для выполнения отложенных действий.
 * В качестве реализации существуют 2 метода,
 * каждый из которых отличается последовательностью
 * выполнений отложенного действия и ожиданием.
 * @author Mikhail Rozdin
 * @version $Id$
 * @since 0.1
 */
public class ActionDelay {

    private void runTimer(long period) {
        long startTime = Calendar.getInstance().getTimeInMillis();
        long finish = startTime + period;
        while (finish > Calendar.getInstance().getTimeInMillis()) {
        }
    }

    /**
     * Вначале ожидается выполнение таймера, затем происходит отложенное действие.
     * @param period время паузы в миллисекундах.
     * @param runnable функциональный интерфейс в качестве отложенного действия.
     */
    public final void  startAfter(long period, Runnable runnable) {
        runTimer(period);
        runnable.run();
    }

    /**
     * Вначале выполняется действие, затем запускается ожидание.
     * @param period время паузы в миллисекундах.
     * @param runnable функциональный интерфейс в качестве отложенного действия.
     */
    public final void startBefore(long period, Runnable runnable) {
        runnable.run();
        runTimer(period);
    }
}


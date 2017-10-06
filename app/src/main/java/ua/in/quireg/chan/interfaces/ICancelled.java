package ua.in.quireg.chan.interfaces;

public interface ICancelled {
    /**
     * Возвращает true, если был вызван метод cancel. Нужно проверять несколько
     * раз в методе doBackground
     */
    boolean isCancelled();
}

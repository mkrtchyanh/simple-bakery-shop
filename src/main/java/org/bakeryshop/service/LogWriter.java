package org.bakeryshop.service;

@FunctionalInterface
public interface LogWriter {
    void write(CharSequence log);
}

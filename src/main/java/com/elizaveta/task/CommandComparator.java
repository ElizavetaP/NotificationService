package com.elizaveta.task;

import java.util.Comparator;

public class CommandComparator implements Comparator<Command>{
    @Override
    public int compare(Command o1, Command o2) {
        return -o1.getTime().compareTo(o2.getTime());
    }
}

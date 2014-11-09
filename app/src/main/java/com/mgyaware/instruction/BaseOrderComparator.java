package com.mgyaware.instruction;

import java.util.Comparator;

public class BaseOrderComparator implements Comparator<BaseOrder> {
    @Override
    public int compare(BaseOrder lhs, BaseOrder rhs) {
        return lhs.order - rhs.order;
    }
}

package com.io.begstd.slot.model.gamerule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@Builder (toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DataCell<T>{

    private T symbol;
    private int type;
    
    private boolean check; //cluster
    private int vertex; //vertex in cluster
    private String info;
    
    @Override
    public String toString() {
        return "DataCell [symbol=" + symbol + ", type=" + type + "]";
    }
    
    public static <T> DataCellBuilder<T> builder(Class<T> clazz) {
        return new DataCellBuilder<T>();
    }
}

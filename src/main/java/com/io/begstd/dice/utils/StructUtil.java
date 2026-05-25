package com.io.begstd.dice.utils;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StructUtil {

    public static <T> Struct convertToStruct(T obj) throws IllegalArgumentException, IllegalAccessException {
        Struct.Builder structBuilder = Struct.newBuilder();
        Value.Builder vb = Value.newBuilder();
        Field[] fields = obj.getClass().getDeclaredFields();
        
        fields = addElementToArray(fields, obj.getClass().getSuperclass().getDeclaredFields());

        Map<String, Value> mapFields = new HashMap<>();

        for (Field field : fields) {
            if(field.getName().startsWith("$$")){
                continue;//skip PIT coverage filed
            }
            field.setAccessible(true);
            if (field.get(obj) != null) {
                if (field.getType().isPrimitive() || field.getType().equals(String.class)) {

                    if (!isZeroData(field.get(obj))) {
                        mapFields.put(field.getName(), getValueOfField(field, obj));
                    }

                } else if (field.get(obj) instanceof Collection) {

                    Collection<?> collection = (Collection<?>) field.get(obj);
                    if (!collection.isEmpty()) {
                        Iterator<?> ite = collection.iterator();
                        mapFields.put(field.getName(), vb.setListValue(getListValueFromIterator(ite)).build());
                    }

                } else if (field.getType().isArray()) {
                    Object objArr = field.get(obj);
                    
                    List<Object> list = new ArrayList<>();
                    for (int i = 0, len = Array.getLength(objArr); i < len; i++) {
                        list.add(Array.get(objArr, i));
                    }
                    
                    if (!list.isEmpty()) {
                        Iterator<?> ite = list.iterator();
                        mapFields.put(field.getName(), vb.setListValue(getListValueFromIterator(ite)).build());
                    }
                    
                } else {
                    mapFields.put(field.getName(), vb.setStructValue(convertToStruct(field.get(obj))).build());
                }
            }
            structBuilder.putAllFields(mapFields);
        }
        return structBuilder.build();
    }

    private static Value getValueOfField(Field field, Object obj)
            throws IllegalArgumentException, IllegalAccessException {
        Value.Builder valueBuilder = Value.newBuilder();

        switch (field.getType().getName()) {
        case "byte":
            valueBuilder.setNumberValue(field.getByte(obj));
            break;
        case "short":
            valueBuilder.setNumberValue(field.getShort(obj));
            break;
        case "int":
            valueBuilder.setNumberValue(field.getInt(obj));
            break;
        case "long":
            valueBuilder.setNumberValue(field.getLong(obj));
            break;
        case "boolean":
            valueBuilder.setBoolValue(field.getBoolean(obj));
            break;
        case "double":
            valueBuilder.setNumberValue(field.getDouble(obj));
            break;
        case "float":
            valueBuilder.setNumberValue(field.getFloat(obj));
            break;
        case "char":
            // TODO
            break;
        case "java.lang.String":
            valueBuilder.setStringValue(field.get(obj).toString());
            break;
        default:
            break;
        }
        return valueBuilder.build();
    }

    private static Value getValueOfField(Object objValue) throws IllegalArgumentException, IllegalAccessException {
        Value.Builder valueBuilder = Value.newBuilder();

        if (objValue instanceof String) {
            valueBuilder.setStringValue((String) objValue);
        } else if (objValue instanceof Number) {
            valueBuilder.setNumberValue(((Number) objValue).doubleValue());
        } else {
            valueBuilder.setStructValue(convertToStruct(objValue));
        }

        return valueBuilder.build();
    }

    private static ListValue getListValueFromIterator(Iterator<?> ite)
            throws IllegalArgumentException, IllegalAccessException {
        ListValue.Builder listValueBuilder = ListValue.newBuilder();
        Object obj;

        while (ite.hasNext()) {
            obj = ite.next();
            listValueBuilder.addValues(getValueOfField(obj));
        }
        return listValueBuilder.build();
    }

    private static boolean isZeroData(Object obj) {
        boolean isZeroData = false;
        if (obj instanceof String) {
            isZeroData = StringUtils.isEmpty(obj);
        } else if (obj instanceof Integer) {
            isZeroData = obj == null || (Integer) obj == 0;
        } else if (obj instanceof Double) {
            isZeroData = obj == null || (Double) obj == 0;
        }
        return isZeroData;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] addElementToArray(T[] arr, T... eles) {
        List<T> list = new LinkedList<>(Arrays.asList(arr));
        list.addAll(Arrays.asList(eles));

        return list.toArray((T[]) Array.newInstance(arr.getClass().getComponentType(), list.size()));
    }

}

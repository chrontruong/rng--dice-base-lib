package com.io.begstd.slot.utils;

import com.google.protobuf.Struct;
import lombok.AllArgsConstructor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
public class StructUtilTest {
    @Test
    public void convertToStructTest() {
        ViewObject viewObj = new ViewObject(0, 1, 0.0, 1.0, null, "", "abc", null, new ArrayList<>(), Arrays.asList(1,2,3), null, new int[] {} , new int[]{1,2,3});
        Struct struct;
        try {
            struct = StructUtil.convertToStruct(viewObj);
            
            Assert.assertTrue(struct.containsFields("notZeroInt"));
            Assert.assertTrue(struct.containsFields("notZeroDouble"));
            Assert.assertTrue(struct.containsFields("notEmptyString"));
            Assert.assertTrue(struct.containsFields("notEmptyList"));
            Assert.assertTrue(struct.containsFields("notEmptyArray"));
            
            Assert.assertFalse(struct.containsFields("zeroInt"));
            Assert.assertFalse(struct.containsFields("zeroDouble"));
            Assert.assertFalse(struct.containsFields("nullString"));
            Assert.assertFalse(struct.containsFields("emptyString"));
            Assert.assertFalse(struct.containsFields("nullList"));
            Assert.assertFalse(struct.containsFields("emptyList"));
            Assert.assertFalse(struct.containsFields("nullArray"));
            Assert.assertFalse(struct.containsFields("emptyArray"));
            
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

@AllArgsConstructor
class ViewObject {

    private int zeroInt;
    private int notZeroInt;
    private double zeroDouble;
    private double notZeroDouble;
    private String nullString;
    private String emptyString;
    private String notEmptyString;
    private List<Integer> nullList;
    private List<Integer> emptyList;
    private List<Integer> notEmptyList;
    private int[] nullArray;
    private int[] emptyArray;
    private int[] notEmptyArray;
}

package com.io.begstd.slot.game.test;

import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

/**
 * Data for stored specific matrix for testing
 *
 */
@Data
@RedisHash("MatrixDataForTest")
public class MatrixDataForTest{

    protected String serviceId;
    protected String userId;
    protected String jackpotType;
    protected String matrixData;
    protected int stackedTypeMode;
    protected DataCell<Symbol>[] matrixDataCell;
    protected List<Integer> tableFormat;
    protected List<Integer> goldenReel;
    protected Integer stackedReel1;
    protected Integer stackedReel5;
    protected String stackedCodeReel1;
    protected String stackedCodeReel5;
    protected String megaSymbolCode;
    protected Integer megaSymbolFormat;
    public String[] getMatrixData() {
        return matrixData.split(",");
    }

    protected String matrixLightningData;
    protected DataCell<Symbol>[] matrixLightningDataCell;
    protected List<Integer> tableLightningFormat;
    public String[] getMatrixLightningData() {
        if (matrixLightningData != null)
            return matrixLightningData.split(",");
        return null;
    }
    protected String powerUpSymbolCode;

    protected String jackpotTypeLeft;
    protected String jackpotTypeRight;
    
}

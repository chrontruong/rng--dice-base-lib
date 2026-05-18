package com.io.begstd.slot;

import com.io.begstd.slot.game.test.MatrixDataForTest;
import com.io.begstd.slot.model.config.SlotMachineConfigForFree;
import com.io.begstd.slot.model.config.SlotMachineConfigForMini;
import com.io.begstd.slot.model.config.SlotMachineConfigForNormal;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SlotBaseTestConfiguration.class})
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public abstract class AbstractBaseSlotMockTest {


    @Rule
    public TestName testName = new TestName();

    protected DataCell<Symbol> K; // Wild symbol
    protected DataCell<Symbol> S2; // normal symbol
    protected DataCell<Symbol> S3; // normal symbol
    protected DataCell<Symbol> S4; // normal symbol
    protected DataCell<Symbol> S5; // normal symbol
    protected DataCell<Symbol> S6; // normal symbol
    protected DataCell<Symbol> S7; // normal symbol
    protected DataCell<Symbol> S8; // normal symbol
    protected DataCell<Symbol> S9; // normal symbol
    protected DataCell<Symbol> S10; // normal symbol
    protected DataCell<Symbol> SJ; // normal symbol
    protected DataCell<Symbol> SR; // bonus symbol
    protected DataCell<Symbol> SA; // scatter symbol

    @Autowired
    @Qualifier("slotMachineConfigForNormal")
    protected SlotMachineConfigForNormal configForNormal;

    @Autowired
    protected SlotMachineConfigForFree configForFree;

    @Autowired
    protected SlotMachineConfigForMini configForMini;

    @Before
    public void init(){
        String[] sSymbols = {
                "K",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10",
                "J",
                "R",
                "A",
        };

        Symbol[] inputSymbol = configForNormal.getSymbolsForTest(sSymbols);
        K = DataCell.builder(Symbol.class).symbol(inputSymbol[0]).build();
        S2 = DataCell.builder(Symbol.class).symbol(inputSymbol[1]).build();
        S3 = DataCell.builder(Symbol.class).symbol(inputSymbol[2]).build();
        S4 = DataCell.builder(Symbol.class).symbol(inputSymbol[3]).build();
        S5 = DataCell.builder(Symbol.class).symbol(inputSymbol[4]).build();
        S6 = DataCell.builder(Symbol.class).symbol(inputSymbol[5]).build();
        S7 = DataCell.builder(Symbol.class).symbol(inputSymbol[6]).build();
        S8 = DataCell.builder(Symbol.class).symbol(inputSymbol[7]).build();
        S9 = DataCell.builder(Symbol.class).symbol(inputSymbol[8]).build();
        S10 = DataCell.builder(Symbol.class).symbol(inputSymbol[9]).build();
        SJ = DataCell.builder(Symbol.class).symbol(inputSymbol[10]).build();
        SR = DataCell.builder(Symbol.class).symbol(inputSymbol[11]).build();
        SA = DataCell.builder(Symbol.class).symbol(inputSymbol[12]).build();
    }

    protected DataCell<Symbol>[] createLine(DataCell<Symbol>... cells){
        return cells;
    }

    @SuppressWarnings("unchecked")
    protected MatrixDataForTest buildDataForTest(String userId, String serviceId,
                                               List<Integer> tableFormat,
                                               DataCell<Symbol>[] line) {
        MatrixDataForTest data = new MatrixDataForTest();
        data.setUserId(userId);
        data.setServiceId(serviceId);
        data.setTableFormat(tableFormat);
        data.setMatrixDataCell(line);
        return data;
    }

}

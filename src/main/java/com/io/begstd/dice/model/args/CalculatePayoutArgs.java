package com.io.begstd.dice.model.args;

import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.gamerule.DenominationLevel;
import com.io.begstd.dice.model.gamerule.InitJackpotChild;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculatePayoutArgs {

    private List<InitJackpotChild> initJackpotList; //only normal
    private DenominationLevel denomLevel;
    private float multiplier;
    private IDiceMachineConfig config;
}

package com.io.begstd.dice.exception;

import com.io.begstd.dice.common.DiceGameMessage;
import com.io.begstd.dice.common.DiceGameMessage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiceGameMessageException extends RuntimeException {

    private static final long serialVersionUID = -2569222191854088789L;
    
    @Getter
    private DiceGameMessage messageCode;

    private final List<String> parameters = new ArrayList<>();

    public DiceGameMessageException(DiceGameMessage message, String userId, String userType, String commandId) {
        this(message, null, userId, userType, commandId);
    }

    public DiceGameMessageException(DiceGameMessage message, Throwable cause, String userId, String userType, String commandId) {
        super(message.name(), cause);
        this.messageCode = message;
        parameters.add(userId);
        parameters.add(userType);
        parameters.add(commandId);
    }

    public List<String> getParameters() {
        return Collections.unmodifiableList(parameters);
    }
    public String getMessageCode() {
        return this.messageCode.getMessageCode();
    }
}

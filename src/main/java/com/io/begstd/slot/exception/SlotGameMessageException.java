package com.io.begstd.slot.exception;

import com.io.begstd.slot.common.SlotGameMessage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SlotGameMessageException extends RuntimeException {

    private static final long serialVersionUID = 460833696011005324L;
    
    @Getter
    private SlotGameMessage messageCode;

    private final List<String> parameters = new ArrayList<>();

    public SlotGameMessageException(SlotGameMessage message, String userId, String userType, String commandId) {
        this(message, null, userId, userType, commandId);
    }

    public SlotGameMessageException(SlotGameMessage message, Throwable cause, String userId, String userType, String commandId) {
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

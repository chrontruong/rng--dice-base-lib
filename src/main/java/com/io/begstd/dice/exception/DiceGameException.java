package com.io.begstd.dice.exception;

import com.io.begstd.dice.common.DiceGameError;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiceGameException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 5754048877227133599L;

    @Getter
    private DiceGameError error;

    private final List<String> parameters = new ArrayList<>();

    public DiceGameException(DiceGameError error, String userId, String userType, String commandId, String... params) {
        this(error, null,userId, userType, commandId, params);
    }

    public DiceGameException(DiceGameError error, Throwable cause, String userId, String userType, String commandId, String... params) {
        super(error.name(), cause);
        this.error = error;
        parameters.add(userId);
        parameters.add(userType);
        parameters.add(commandId);
        Collections.addAll(this.parameters, params);
    }

    public String getErrorCode() {
        return error.getErrorCode();
    }

    public List<String> getParameters() {
        return Collections.unmodifiableList(parameters);
    }
}

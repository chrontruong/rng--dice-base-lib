package com.io.begstd.slot.exception;

import com.io.begstd.slot.common.SlotGameError;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SlotGameException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 6897687551814362172L;

    @Getter
    private SlotGameError error;

    private final List<String> parameters = new ArrayList<>();

    public SlotGameException(SlotGameError error, String userId, String userType, String commandId, String... params) {
        this(error, null,userId, userType, commandId, params);
    }

    public SlotGameException(SlotGameError error, Throwable cause, String userId, String userType, String commandId, String... params) {
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

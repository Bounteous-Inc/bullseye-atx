package com.bullseyeaem.core.common.exceptions;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BullseyeException extends RuntimeException {
    private String displayableMessage;

    public BullseyeException(String message) {
        super(message);
        this.displayableMessage = EMPTY;
    }

    public BullseyeException(String message, Throwable cause) {
        super(message, cause);
        this.displayableMessage = EMPTY;
    }

    public BullseyeException(String message, String displayableMessage) {
        super(message);
        this.displayableMessage = displayableMessage;
    }

    public BullseyeException(String message, String displayableMessage, Throwable cause) {
        super(message, cause);
        this.displayableMessage = displayableMessage;
    }

    public boolean hasDisplayableMessage() {
        return isNotBlank(this.displayableMessage);
    }
}

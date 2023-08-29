package at.ac.tuwien.sepm.groupphase.backend.exception;

import java.util.List;

public class ConflictException extends ErrorListException {
    public ConflictException(String messageSummary, List<String> errors) {
        super("Conflicts", messageSummary, errors);
    }
}

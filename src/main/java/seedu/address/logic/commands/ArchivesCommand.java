package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.CommandHistory;
import seedu.address.model.Model;
import seedu.address.model.ModelContext;

/**
 * Lists all entries in the archives to the user.
 */
public class ArchivesCommand extends Command {

    public static final String COMMAND_WORD = "archives";
    public static final String COMMAND_ALIAS = "archs";

    public static final String MESSAGE_SUCCESS = "Context switched to archive-context. Listed all entries in archives.";


    @Override
    public CommandResult execute(Model model, CommandHistory history) {
        requireNonNull(model);
        model.setContext(ModelContext.CONTEXT_ARCHIVE);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
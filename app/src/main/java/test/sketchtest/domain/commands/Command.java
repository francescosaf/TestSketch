package test.sketchtest.domain.commands;

/**
 * Created by karimmastrobuono on 8/25/14.
 */
public interface Command  {
    public void execute();
    public void undo();
}

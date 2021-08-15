package scripts.api.events;

import org.tribot.api2007.Login;
import org.tribot.api2007.Login.STATE;
import org.tribot.script.Script;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public abstract class BotEvent {

    private boolean complete = false;
    private boolean failed = true;

    protected Supplier<Boolean> interruptCondition;

    private final ArrayList<Supplier<Boolean>> conditionList = new ArrayList<>();

    protected Script script;

    public BotEvent(Script script) {
        super();
        this.script = script;
    }

    public BotEvent setInterruptCondition(Supplier<Boolean> interruptCondition) {
        this.interruptCondition = interruptCondition;
        return this;
    }

    public BotEvent reset() {
        this.complete = false;
        this.failed = true;
        return this;
    }

    public abstract void step() throws InterruptedException, IOException;

    public void setComplete() {
        this.complete = true;
        this.failed = false;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isFailed() {
        return failed;
    }

    public void execute() throws InterruptedException, IOException {
        executed();
    }

    public boolean executed() throws InterruptedException, IOException {
        while (!isComplete() && script.isActive() && !script.isPaused()) {
            if (Login.getLoginState().equals(STATE.INGAME)) {
                for (Supplier<Boolean> condition : conditionList) {
                    if (!condition.get()) {
                        setComplete();
                        return true;
                    }
                }
                if (interruptCondition != null && interruptCondition.get()) {
                    setComplete();
                } else if (!script.isPaused()) {
                    step();
                }
            }
        }
        return isComplete() && !isFailed();
    }

    public static <T> T errorWrapper(Callable<T> event) throws Exception {
        try {
            return event.call();
        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e){
            e.printStackTrace();
        } return null;
    }

}

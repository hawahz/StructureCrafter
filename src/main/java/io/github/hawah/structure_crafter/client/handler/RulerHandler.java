package io.github.hawah.structure_crafter.client.handler;

public class RulerHandler implements IHandler {



    @Override
    public void tick() {
        if (!isVisible()) {

        }

        if (!isActive()) {
            return;
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }
}

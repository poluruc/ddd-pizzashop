package com.mattstine.dddworkshop.pizzashop.kitchen;

import com.mattstine.dddworkshop.pizzashop.infrastructure.events.adapters.InProcessEventLog;
import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.EventLog;
import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.Topic;
import com.mattstine.dddworkshop.pizzashop.infrastructure.repository.ports.Aggregate;
import com.mattstine.dddworkshop.pizzashop.infrastructure.repository.ports.AggregateState;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.function.BiFunction;

@Value
public final class Pizza implements Aggregate {
    PizzaRef ref;
    KitchenOrderRef kitchenOrderRef;
    Size size;
    EventLog $eventLog;
    @NonFinal
    State state;

    @Builder
    private Pizza(@NonNull PizzaRef ref,
                  @NonNull KitchenOrderRef kitchenOrderRef,
                  @NonNull Size size,
                  @NonNull EventLog eventLog) {
        this.ref = ref;
        this.kitchenOrderRef = kitchenOrderRef;
        this.size = size;
        this.$eventLog = eventLog;

        this.state = State.NEW;
    }

    /**
     * Private no-args ctor to support reflection ONLY.
     */
    @SuppressWarnings("unused")
    private Pizza() {
        this.ref = null;
        this.kitchenOrderRef = null;
        this.size = null;
        this.$eventLog = null;
    }

    public boolean isNew() {
        return this.state == State.NEW;
    }

    void startPrep() {
        if (this.state != State.NEW) {
            throw new IllegalStateException("Can only prep NEW Pizza");
        }

        assert this.state != null;
        this.state = State.PREPPING;

        assert $eventLog != null;
        $eventLog.publish(new Topic("pizzas"), new PizzaPrepStartedEvent(ref));
    }

    boolean isPrepping() {
        return this.state == State.PREPPING;
    }

    void finishPrep() {
        if (this.state != State.PREPPING) {
            throw new IllegalStateException("Can only finish prep on PREPPING Pizza");
        }

        assert this.state != null;
        this.state = State.PREPPED;

        assert $eventLog != null;
        $eventLog.publish(new Topic("pizzas"), new PizzaPrepFinishedEvent(ref));
    }

    boolean hasFinishedPrep() {
        return this.state == State.PREPPED;
    }

    void startBake() {
        if (this.state != State.PREPPED) {
            throw new IllegalStateException("Can only bake PREPPED Pizza");
        }

        assert this.state != null;
        this.state = State.BAKING;

        assert $eventLog != null;
        $eventLog.publish(new Topic("pizzas"), new PizzaBakeStartedEvent(ref));
    }

    boolean isBaking() {
        return this.state == State.BAKING;
    }

    void finishBake() {
        if (this.state != State.BAKING) {
            throw new IllegalStateException("Can only finish BAKING Pizza");
        }

        assert this.state != null;
        this.state = State.BAKED;

        assert $eventLog != null;
        $eventLog.publish(new Topic("pizzas"), new PizzaBakeFinishedEvent(ref));
    }

    boolean hasFinishedBaking() {
        return this.state == State.BAKED;
    }

    @Override
    public Pizza identity() {
        return Pizza.builder()
                .eventLog(EventLog.IDENTITY)
                .ref(PizzaRef.IDENTITY)
                // .state(State.NEW)
                .build();
    }

    @Override
    public BiFunction<Pizza, PizzaEvent, Pizza> accumulatorFunction() {
        return new Accumulator();
    }

    @Override
    public PizzaRef getRef() {
        return ref;
    }

    @Override
    public PizzaState state() {
        return new PizzaState(ref, kitchenOrderRef, this.size, state);
    }

    enum Size {
        IDENTITY, SMALL, MEDIUM, LARGE
    }

    enum State {
        NEW,
        PREPPING,
        PREPPED,
        BAKING,
        BAKED
    }

    private static class Accumulator implements BiFunction<Pizza, PizzaEvent, Pizza> {

        @Override
        public Pizza apply(Pizza pizza, PizzaEvent pizzaEvent) {
            if (pizzaEvent instanceof PizzaAddedEvent) {
                PizzaAddedEvent pae = (PizzaAddedEvent) pizzaEvent;
                PizzaState pizzaState = pae.getState();
                return Pizza.builder()
                        .eventLog(InProcessEventLog.instance())
                        .ref(pizzaState.getRef())
                        .kitchenOrderRef(pizzaState.getKitchenOrderRef())
                        .size(pizzaState.getSize())
                        // .state(pizzaState.getState())
                        .build();
            }
            throw new IllegalStateException("Unknown PizzaEvent");
        }        
    }

    @Value
    static class PizzaState implements AggregateState {
        private final PizzaRef ref;
        private final KitchenOrderRef kitchenOrderRef;
        private final Size size;
        private final State state;
    }
}

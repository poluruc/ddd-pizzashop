package com.mattstine.dddworkshop.pizzashop.kitchen;

import com.mattstine.dddworkshop.pizzashop.infrastructure.events.adapters.InProcessEventLog;
import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.EventLog;
import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.Topic;
import com.mattstine.dddworkshop.pizzashop.infrastructure.repository.ports.Aggregate;
import com.mattstine.dddworkshop.pizzashop.infrastructure.repository.ports.AggregateState;
import com.mattstine.dddworkshop.pizzashop.ordering.OnlineOrderRef;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.List;
import java.util.function.BiFunction;

@Value
public final class KitchenOrder implements Aggregate {
    KitchenOrderRef ref;
    OnlineOrderRef onlineOrderRef;
    List<Pizza> pizzas;
    EventLog $eventLog;
    @NonFinal
    State state;

    @Builder
    private KitchenOrder(@NonNull KitchenOrderRef ref, @NonNull OnlineOrderRef onlineOrderRef, @Singular List<Pizza> pizzas, @NonNull EventLog eventLog) {
        this.ref = ref;
        this.onlineOrderRef = onlineOrderRef;
        this.pizzas = pizzas;
        this.$eventLog = eventLog;

        this.state = State.NEW;
    }

    /**
     * Private no-args ctor to support reflection ONLY.
     */
    @SuppressWarnings("unused")
    private KitchenOrder() {
        this.ref = null;
        this.onlineOrderRef = null;
        this.pizzas = null;
        this.$eventLog = null;
    }

    public boolean isNew() {
        return this.state == State.NEW;
    }

    void startPrep() {
        if (this.state != State.NEW) {
            throw new IllegalStateException("Can only prep NEW KitchenOrder");
        }

        this.state = State.PREPPING;

        assert $eventLog != null;
        $eventLog.publish(new Topic("kitchen_orders"), new KitchenOrderPrepStartedEvent(ref));
    }

    boolean isPrepping() {
        return this.state == State.PREPPING;
    }

    void startBake() {
         if (this.state != State.PREPPING) {
            throw new IllegalStateException("Can only prep NEW KitchenOrder");
        }

        this.state = State.BAKING;

        assert $eventLog != null;
        $eventLog.publish(new Topic("kitchen_orders"), new KitchenOrderBakeStartedEvent(ref));
    }

    boolean isBaking() {
        return this.state == State.BAKING;
    }

    void startAssembly() {
         if (this.state != State.BAKING) {
            throw new IllegalStateException("Can only assemble BAKING KitchenOrder");
        }

        this.state = State.ASSEMBLING;

        assert $eventLog != null;
        $eventLog.publish(new Topic("kitchen_orders"), new KitchenOrderAssemblyStartedEvent(ref));
    }

    boolean hasStartedAssembly() {
        return this.state == State.ASSEMBLING;
    }

    void finishAssembly() {
         if (this.state != State.ASSEMBLING) {
            throw new IllegalStateException("Can only finish assembly of ASSEMBLING KitchenOrder");
        }

        this.state = State.ASSEMBLED;

        assert $eventLog != null;
        $eventLog.publish(new Topic("kitchen_orders"), new KitchenOrderAssemblyFinishedEvent(ref));
    }

    boolean hasFinishedAssembly() {
        return this.state == State.ASSEMBLED;
    }

    @Override
    public KitchenOrder identity() {
        return KitchenOrder.builder()
                .eventLog(EventLog.IDENTITY)
                .ref(KitchenOrderRef.IDENTITY)
                .onlineOrderRef(OnlineOrderRef.IDENTITY)
                // .state(State.NEW)
                .build();
    }

    @Override
    public BiFunction<KitchenOrder, KitchenOrderEvent, KitchenOrder> accumulatorFunction() {
        return new Accumulator();
    }

    @Override
    public OrderState state() {
        return new OrderState(ref, onlineOrderRef, pizzas, state);
    }

    enum State {
        NEW,
        PREPPING,
        BAKING,
        ASSEMBLING,
        ASSEMBLED
    }

  private static class Accumulator implements BiFunction<KitchenOrder, KitchenOrderEvent, KitchenOrder> {

        @Override
        public KitchenOrder apply(KitchenOrder kitchenOrder, KitchenOrderEvent kitchenOrderEvent) {
            // implement accumulator function here
            if (kitchenOrderEvent instanceof KitchenOrderAddedEvent) {
                KitchenOrderAddedEvent e = (KitchenOrderAddedEvent) kitchenOrderEvent;
                return KitchenOrder.builder()
                        .ref(e.getRef())
                        .onlineOrderRef(e.getState().getOnlineOrderRef())
                        .pizzas(e.getState().getPizzas())
                        .eventLog(InProcessEventLog.instance())
                        .build();
            } else if (kitchenOrderEvent instanceof KitchenOrderPrepStartedEvent) {
                KitchenOrderPrepStartedEvent e = (KitchenOrderPrepStartedEvent) kitchenOrderEvent;
                KitchenOrder k = KitchenOrder.builder()
                        .ref(kitchenOrder.ref)
                        .onlineOrderRef(kitchenOrder.onlineOrderRef)
                        .pizzas(kitchenOrder.pizzas)
                        .eventLog(kitchenOrder.$eventLog)
                        .build();
                        k.state= State.PREPPING;
                        return k;
            } else if(kitchenOrderEvent instanceof KitchenOrderBakeStartedEvent) {
                KitchenOrder k = KitchenOrder.builder()
                        .ref(kitchenOrder.ref)
                        .onlineOrderRef(kitchenOrder.onlineOrderRef)
                        .pizzas(kitchenOrder.pizzas)
                        .eventLog(kitchenOrder.$eventLog)
                        .build();
                        k.state= State.BAKING;
                        return k;
            } else if(kitchenOrderEvent instanceof KitchenOrderAssemblyStartedEvent) {
                KitchenOrder k = KitchenOrder.builder()
                        .ref(kitchenOrder.ref)
                        .onlineOrderRef(kitchenOrder.onlineOrderRef)
                        .pizzas(kitchenOrder.pizzas)
                        .eventLog(kitchenOrder.$eventLog)
                        .build();
                        k.state= State.ASSEMBLING;
                        return k;
            } else if(kitchenOrderEvent instanceof KitchenOrderAssemblyFinishedEvent) {
                KitchenOrder k = KitchenOrder.builder()
                        .ref(kitchenOrder.ref)
                        .onlineOrderRef(kitchenOrder.onlineOrderRef)
                        .pizzas(kitchenOrder.pizzas)
                        .eventLog(kitchenOrder.$eventLog)
                        .build();
                        k.state= State.ASSEMBLED;
                        return k;
            } else {
                throw new IllegalStateException("Unknown event type: " + kitchenOrderEvent.getClass().getSimpleName());
            }

    }
    }

    /*
     * Pizza Value Object for OnlineOrder Details Only
     */
    @Value
    public static final class Pizza {
        Size size;

        @Builder
        private Pizza(@NonNull Size size) {
            this.size = size;
        }

        public enum Size {
            SMALL, MEDIUM, LARGE
        }
    }

    @Value
    static class OrderState implements AggregateState {
        private final KitchenOrderRef ref;
        private final OnlineOrderRef onlineOrderRef;
        private final List<Pizza> pizzas;
        private final State state;
    }
}

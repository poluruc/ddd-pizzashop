package com.mattstine.dddworkshop.pizzashop.kitchen;

import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.EventLog;
import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.Topic;
import com.mattstine.dddworkshop.pizzashop.infrastructure.repository.adapters.InProcessEventSourcedRepository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class InProcessEventSourcedPizzaRepository extends InProcessEventSourcedRepository<PizzaRef, Pizza, Pizza.PizzaState, PizzaEvent, PizzaAddedEvent> implements PizzaRepository {

    private Map<KitchenOrderRef, Set<Pizza>> kitchenOrderRefToPizzas = new java.util.concurrent.ConcurrentHashMap<>();
    // a. Get List of PizzaRefs (Kitchen order ones) given a KitchenOrderRef
    // b. Get List of PizzaRefs (Pizza aggregate ones)/ or the aggregates themselves  given the list above

    InProcessEventSourcedPizzaRepository(EventLog eventLog, Topic pizzas) {
        super(eventLog, PizzaRef.class, Pizza.class, Pizza.PizzaState.class, PizzaAddedEvent.class, pizzas);

        eventLog.subscribe(pizzas, e -> {
            if (e instanceof PizzaAddedEvent) {
                PizzaAddedEvent koae = (PizzaAddedEvent) e;
                kitchenOrderRefToPizzas.putIfAbsent(koae.getState().getKitchenOrderRef(), new HashSet<>());
                kitchenOrderRefToPizzas.get(koae.getState().getKitchenOrderRef()).add(findByRef(koae.getRef()));
            }
        });
    }

    @Override
    public Set<Pizza> findPizzasByKitchenOrderRef(KitchenOrderRef kitchenOrderRef) {
        return kitchenOrderRefToPizzas.get(kitchenOrderRef);
    }
}

package com.mattstine.dddworkshop.pizzashop.kitchen;

import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.EventLog;
import com.mattstine.dddworkshop.pizzashop.infrastructure.events.ports.Topic;
import com.mattstine.dddworkshop.pizzashop.ordering.OnlineOrderRef;
import com.mattstine.dddworkshop.pizzashop.ordering.OrderingService;

// import com.mattstine.dddworkshop.pizzashop.ordering.OnlineOrderSubmittedEvent;

import lombok.Value;

import java.util.Set;

@Value
final class DefaultKitchenService implements KitchenService {
	EventLog eventLog;
	KitchenOrderRepository kitchenOrderRepository;
	PizzaRepository pizzaRepository;
	OrderingService orderingService;

	DefaultKitchenService(EventLog eventLog, KitchenOrderRepository kitchenOrderRepository, PizzaRepository pizzaRepository, OrderingService orderingService) {
		this.kitchenOrderRepository = kitchenOrderRepository;
		this.eventLog = eventLog;
		this.pizzaRepository = pizzaRepository;
		this.orderingService = orderingService;
		
		// start order prep on receipt of KitchenOrderAddedEvent
		eventLog.subscribe(new Topic("kitchen"), e -> {
            if (e instanceof KitchenOrderAddedEvent) {
                KitchenOrderAddedEvent pse = (KitchenOrderAddedEvent) e;
                this.startOrderPrep(pse.getRef());
            }
        });

		// start order prep on receipt of OnlineOrderSubmittedEvent
		// eventLog.subscribe(new Topic("ordering"), e -> {
        //     if (e instanceof OnlineOrderSubmittedEvent) {
        //         OnlineOrderSubmittedEvent pse = (OnlineOrderSubmittedEvent) e;
        //         this.startOrderPrep(pse.getRef());
        //     }
        // });
		// $eventLog.publish(new Topic("ordering"), new OnlineOrderSubmittedEvent(ref));

	}

	@Override
	public void startOrderPrep(KitchenOrderRef kitchenOrderRef) {
	}

	@Override
	public void finishPizzaPrep(PizzaRef ref) {
	}

	@Override
	public void removePizzaFromOven(PizzaRef ref) {
	}

	@Override
	public KitchenOrder findKitchenOrderByRef(KitchenOrderRef kitchenOrderRef) {
		return null;
	}

	@Override
	public KitchenOrder findKitchenOrderByOnlineOrderRef(OnlineOrderRef onlineOrderRef) {
		return null;
	}

	@Override
	public Pizza findPizzaByRef(PizzaRef ref) {
		return null;
	}

	@Override
	public Set<Pizza> findPizzasByKitchenOrderRef(KitchenOrderRef kitchenOrderRef) {
		return null;
	}

}

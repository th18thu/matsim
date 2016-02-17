package org.matsim.core.events;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.events.handler.EventHandler;

import javax.inject.Inject;
import java.util.Set;

public class EventsManagerModule extends AbstractModule {

	@Override
	public void install() {
		bindEventsManager().to(ParallelEventsManager.class).asEagerSingleton();
		bind(EventHandlerRegistrator.class).asEagerSingleton();
	}

	private static class EventHandlerRegistrator {
		@Inject
		EventHandlerRegistrator(EventsManager eventsManager, Set<EventHandler> eventHandlersDeclaredByModules) {
			for (EventHandler eventHandler : eventHandlersDeclaredByModules) {
				eventsManager.addHandler(eventHandler);
			}
		}
	}
}

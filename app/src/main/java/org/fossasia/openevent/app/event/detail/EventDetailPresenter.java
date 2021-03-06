package org.fossasia.openevent.app.event.detail;

import org.fossasia.openevent.app.data.contract.IEventRepository;
import org.fossasia.openevent.app.data.models.Attendee;
import org.fossasia.openevent.app.data.models.Event;
import org.fossasia.openevent.app.data.models.Ticket;
import org.fossasia.openevent.app.event.detail.contract.IEventDetailPresenter;
import org.fossasia.openevent.app.event.detail.contract.IEventDetailView;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class EventDetailPresenter implements IEventDetailPresenter {

    private Event initialEvent;
    private Event event;
    private IEventDetailView eventDetailView;
    private IEventRepository eventRepository;

    private long totalAttendees;
    private long checkedInAttendees;

    @Inject
    public EventDetailPresenter(IEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void attach(IEventDetailView eventDetailView, Event initialEvent) {
        this.eventDetailView = eventDetailView;
        this.initialEvent = initialEvent;
    }

    @Override
    public void start() {
        showEventInfo(initialEvent);

        loadAttendees(initialEvent.getId(), false);
        loadTickets(initialEvent.getId(), false);
    }

    @Override
    public void detach() {
        eventDetailView = null;
    }

    @Override
    public void loadTickets(long eventId, boolean forceReload) {
        if(eventDetailView == null)
            return;

        eventDetailView.showProgressBar(true);

        eventRepository
            .getEvent(eventId, forceReload)
            .subscribe(this::processEventAndDisplay,
                throwable -> {
                    if(eventDetailView == null)
                        return;
                    eventDetailView.showEventLoadError(throwable.getMessage());
                    eventDetailView.showProgressBar(false);
                });
    }

    private void processEventAndDisplay(Event event) {
        if(eventDetailView == null)
            return;

        this.event = event;

        showEventInfo(event);

        List<Ticket> tickets = event.getTickets();

        long totalTickets = 0;
        if(tickets != null) {
            for (Ticket thisTicket : tickets)
                totalTickets += thisTicket.getQuantity();
        }

        event.totalTickets.set(totalTickets);
        event.totalAttendees.set(totalAttendees);
        event.checkedIn.set(checkedInAttendees);

        eventDetailView.showProgressBar(false);
    }

    private void showEventInfo(Event event) {
        if(eventDetailView == null)
            return;

        eventDetailView.showEvent(event);

        String[] startDate = event.getStartTime().split("T");
        String[] endDate = event.getEndTime().split("T");

        event.startDate.set(startDate[0]);
        event.endDate.set(endDate[0]);
        event.eventStartTime.set(endDate[1]);
    }

    @Override
    public void loadAttendees(long eventId, boolean forceReload) {
        if(eventDetailView == null)
            return;

        eventRepository
            .getAttendees(eventId, forceReload)
            .subscribe(this::processAttendeesAndDisplay,
                throwable -> {
                    if(eventDetailView == null)
                        return;
                    eventDetailView.showEventLoadError(throwable.getMessage());
                });
    }

    private void processAttendeesAndDisplay(List<Attendee> attendees) {
        if(eventDetailView == null)
            return;

        totalAttendees = attendees.size();

        if(event != null)
            event.totalAttendees.set(totalAttendees);

        Observable.fromIterable(attendees)
            .filter(Attendee::isCheckedIn)
            .toList()
            .map(List::size)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(checkedIn -> {
                if (eventDetailView == null)
                    return;

                checkedInAttendees = checkedIn;

                if(event != null)
                    event.checkedIn.set(checkedInAttendees);
            });
    }

    public IEventDetailView getView() {
        return eventDetailView;
    }

    public Event getEvent() {
        return event;
    }
}

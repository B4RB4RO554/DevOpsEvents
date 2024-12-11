package tn.esprit.eventsproject.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventServicesImpl implements IEventServices {

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final LogisticsRepository logisticsRepository;

    @Override
    public Participant addParticipant(Participant participant) {
        log.info("Adding participant: {}", participant);
        Participant savedParticipant = participantRepository.save(participant);
        log.info("Participant added with ID: {}", savedParticipant.getIdPart());
        return savedParticipant;
    }

    @Override
    public Event addAffectEvenParticipant(Event event, int idParticipant) {
        log.info("Assigning participant with ID: {} to event: {}", idParticipant, event);
        Participant participant = participantRepository.findById(idParticipant).orElse(null);
        if (participant == null) {
            log.error("Participant with ID: {} not found", idParticipant);
            return null;
        }

        if (participant.getEvents() == null) {
            Set<Event> events = new HashSet<>();
            events.add(event);
            participant.setEvents(events);
        } else {
            participant.getEvents().add(event);
        }
        Event savedEvent = eventRepository.save(event);
        log.info("Event saved with participant assigned. Event ID: {}", savedEvent.getIdEvent());
        return savedEvent;
    }

    @Override
    public Event addAffectEvenParticipant(Event event) {
        log.info("Assigning participants to event: {}", event);
        Set<Participant> participants = event.getParticipants();
        for (Participant aParticipant : participants) {
            Participant participant = participantRepository.findById(aParticipant.getIdPart()).orElse(null);
            if (participant == null) {
                log.warn("Participant with ID: {} not found", aParticipant.getIdPart());
                continue;
            }

            if (participant.getEvents() == null) {
                Set<Event> events = new HashSet<>();
                events.add(event);
                participant.setEvents(events);
            } else {
                participant.getEvents().add(event);
            }
        }
        Event savedEvent = eventRepository.save(event);
        log.info("Event saved with all participants assigned. Event ID: {}", savedEvent.getIdEvent());
        return savedEvent;
    }

    @Override
    public Logistics addAffectLog(Logistics logistics, String descriptionEvent) {
        log.info("Assigning logistics: {} to event with description: {}", logistics, descriptionEvent);
        Event event = eventRepository.findByDescription(descriptionEvent);
        if (event == null) {
            log.error("Event with description: {} not found", descriptionEvent);
            return null;
        }

        if (event.getLogistics() == null) {
            Set<Logistics> logisticsSet = new HashSet<>();
            logisticsSet.add(logistics);
            event.setLogistics(logisticsSet);
            eventRepository.save(event);
            log.info("Logistics set initialized and added to event: {}", event);
        } else {
            event.getLogistics().add(logistics);
            log.info("Logistics added to existing event: {}", event);
        }

        Logistics savedLogistics = logisticsRepository.save(logistics);
        log.info("Logistics saved with ID: {}", savedLogistics.getIdLog());
        return savedLogistics;
    }

    @Override
    public List<Logistics> getLogisticsDates(LocalDate date_debut, LocalDate date_fin) {
        log.info("Fetching logistics for events between dates: {} and {}", date_debut, date_fin);
        List<Event> events = eventRepository.findByDateDebutBetween(date_debut, date_fin);

        List<Logistics> logisticsList = new ArrayList<>();
        for (Event event : events) {
            log.info("Processing event: {}", event);
            if (event.getLogistics().isEmpty()) {
                log.warn("No logistics found for event: {}", event);
                continue;
            }

            for (Logistics logistics : event.getLogistics()) {
                if (logistics.isReserve()) {
                    logisticsList.add(logistics);
                    log.info("Reserved logistics added: {}", logistics);
                }
            }
        }
        log.info("Total logistics fetched: {}", logisticsList.size());
        return logisticsList;
    }

    @Scheduled(cron = "*/60 * * * * *")
    @Override
    public void calculCout() {
        log.info("Calculating costs for events with participant Tounsi Ahmed and role ORGANISATEUR");
        List<Event> events = eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache("Tounsi", "Ahmed", Tache.ORGANISATEUR);

        float somme;
        for (Event event : events) {
            log.info("Processing event: {}", event);
            somme = 0f;
            for (Logistics logistics : event.getLogistics()) {
                if (logistics.isReserve()) {
                    somme += logistics.getPrixUnit() * logistics.getQuantite();
                    log.info("Adding cost for logistics: {}", logistics);
                }
            }
            event.setCout(somme);
            eventRepository.save(event);
            log.info("Total cost for event {}: {}", event.getDescription(), somme);
        }
    }
}

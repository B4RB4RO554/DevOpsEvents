package tn.esprit.eventsproject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;
import tn.esprit.eventsproject.services.EventServicesImpl;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddParticipant() {
        Participant participant = new Participant();
        participant.setIdPart(1);
        when(participantRepository.save(any(Participant.class))).thenReturn(participant);

        Participant savedParticipant = eventServices.addParticipant(participant);

        assertNotNull(savedParticipant);
        assertEquals(1, savedParticipant.getIdPart());
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    void testAddAffectEvenParticipantById() {
        Participant participant = new Participant();
        participant.setIdPart(1);
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        Event event = new Event();
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event savedEvent = eventServices.addAffectEvenParticipant(event, 1);

        assertNotNull(savedEvent);
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectEvenParticipantAll() {
        Participant participant = new Participant();
        participant.setIdPart(1);
        Set<Participant> participants = new HashSet<>();
        participants.add(participant);
        Event event = new Event();
        event.setParticipants(participants);
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event savedEvent = eventServices.addAffectEvenParticipant(event);

        assertNotNull(savedEvent);
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectLog() {
        Event event = new Event();
        when(eventRepository.findByDescription("Test Event")).thenReturn(event);
        Logistics logistics = new Logistics();
        logistics.setIdLog(1);
        when(logisticsRepository.save(any(Logistics.class))).thenReturn(logistics);

        Logistics savedLogistics = eventServices.addAffectLog(logistics, "Test Event");

        assertNotNull(savedLogistics);
        assertEquals(1, savedLogistics.getIdLog());
        verify(eventRepository, times(1)).findByDescription("Test Event");
        verify(logisticsRepository, times(1)).save(logistics);
    }

    @Test
    void testGetLogisticsDates() {
        Event event = new Event();
        Logistics logistics = new Logistics();
        logistics.setReserve(true);
        Set<Logistics> logisticsSet = new HashSet<>();
        logisticsSet.add(logistics);
        event.setLogistics(logisticsSet);
        when(eventRepository.findByDateDebutBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(event));

        List<Logistics> logisticsList = eventServices.getLogisticsDates(LocalDate.now(), LocalDate.now());

        assertNotNull(logisticsList);
        assertEquals(1, logisticsList.size());
        verify(eventRepository, times(1)).findByDateDebutBetween(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testCalculCout() {
        Event event = new Event();
        event.setDescription("Test Event");
        Logistics logistics = new Logistics();
        logistics.setReserve(true);
        logistics.setPrixUnit(100);
        logistics.setQuantite(2);
        Set<Logistics> logisticsSet = new HashSet<>();
        logisticsSet.add(logistics);
        event.setLogistics(logisticsSet);
        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(Collections.singletonList(event));

        eventServices.calculCout();

        verify(eventRepository, times(1)).save(event);
        assertEquals(200, event.getCout());
    }

    @Test
    void testParticipantAndEventRelationship() {
        Participant participant = new Participant();
        participant.setIdPart(1);
        participant.setNom("John");
        participant.setPrenom("Doe");
        participant.setTache(Tache.INVITE);

        Event event = new Event();
        event.setIdEvent(1);
        event.setDescription("Event Description");
        Set<Event> events = new HashSet<>();
        events.add(event);
        participant.setEvents(events);

        when(participantRepository.save(any(Participant.class))).thenReturn(participant);

        Participant savedParticipant = eventServices.addParticipant(participant);
        assertNotNull(savedParticipant);
        assertEquals(1, savedParticipant.getIdPart());
        assertEquals(1, savedParticipant.getEvents().size());
        assertTrue(savedParticipant.getEvents().contains(event));
    }

    @Test
    void testEventAndLogisticsRelationship() {
        Logistics logistics = new Logistics();
        logistics.setIdLog(1);
        logistics.setDescription("Logistics Description");
        logistics.setReserve(true);
        logistics.setPrixUnit(50);
        logistics.setQuantite(5);

        Event event = new Event();
        event.setIdEvent(1);
        event.setDescription("Event Description");
        Set<Logistics> logisticsSet = new HashSet<>();
        logisticsSet.add(logistics);
        event.setLogistics(logisticsSet);

        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event savedEvent = eventRepository.save(event);
        assertNotNull(savedEvent);
        assertEquals(1, savedEvent.getIdEvent());
        assertEquals(1, savedEvent.getLogistics().size());
        assertTrue(savedEvent.getLogistics().contains(logistics));
    }
}

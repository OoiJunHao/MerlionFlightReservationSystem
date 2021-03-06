/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package ejb.session.stateless;

import entity.FareEntity;
import entity.FlightEntity;
import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import entity.SeatInventoryEntity;
import enumeration.CabinClassTypeEnum;
import exceptions.CabinClassNotFoundException;
import exceptions.FlightNotFoundException;
import exceptions.FlightScheduleNotFoundException;
import exceptions.InputDataValidationException;
import exceptions.SeatInventoryNotFoundException;
import exceptions.UpdateFlightScheduleException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javafx.util.Pair;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * @author Ong Bik Jeun
 * @author Ooi Jun Hao
 */
@Stateless
public class FlightScheduleSessionBean implements FlightScheduleSessionBeanRemote, FlightScheduleSessionBeanLocal {

    @EJB
    private FlightSessionBeanLocal flightSessionBean;

    @EJB
    private SeatsInventorySessionBeanLocal seatsInventorySessionBean;
 
    @PersistenceContext(unitName = "MerlionFlightReservationSystem-ejbPU")
    private EntityManager em;
    
    private final ValidatorFactory validatorFactory;
    private final Validator validator;
    
    public FlightScheduleSessionBean() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    // only exposed in local interface => FlightSchedulePlan passed in is managed
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public FlightScheduleEntity createNewSchedule(FlightSchedulePlanEntity flightSchedulePlan, FlightScheduleEntity schedule) throws InputDataValidationException {
         Set<ConstraintViolation<FlightScheduleEntity>> constraintViolations = validator.validate(schedule);
         if(constraintViolations.isEmpty()) {
                em.persist(schedule);
                
                schedule.setFlightSchedulePlan(flightSchedulePlan);
                if (!flightSchedulePlan.getFlightSchedule().contains(schedule)) {
                    flightSchedulePlan.getFlightSchedule().add(schedule);
                }

                return schedule;
                
        } else {
            throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
        }
    }
    
    private String prepareInputDataValidationErrorsMessage(Set<ConstraintViolation<FlightScheduleEntity>> constraintViolations) {
        String msg = "Input data validation error!:";
            
        for(ConstraintViolation constraintViolation:constraintViolations) {
            msg += "\n\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage();
        }
        
        return msg;
    } 
    
    @Override
    public FlightScheduleEntity retrieveFlightScheduleById(Long flightScheduleID) throws FlightScheduleNotFoundException {
        FlightScheduleEntity schedule = em.find(FlightScheduleEntity.class, flightScheduleID);
        
        if(schedule != null) {
            return schedule;
        } else {
            throw new FlightScheduleNotFoundException("Flight Schedule " + flightScheduleID + " not found!");      
        }
        
    }
    
    @Override
    public FlightScheduleEntity retrieveFlightScheduleByIdUnmanaged(Long flightScheduleID) throws FlightScheduleNotFoundException {
        FlightScheduleEntity schedule = em.find(FlightScheduleEntity.class, flightScheduleID);
        
        if(schedule != null) {
            em.detach(schedule);
            return schedule;
        } else {
            throw new FlightScheduleNotFoundException("Flight Schedule " + flightScheduleID + " not found!");      
        }
        
    }
    
    // only exposed in local (managed instances passed in)
    @Override
    public void deleteSchedule(List<FlightScheduleEntity> flightSchedule) {
       
        for(FlightScheduleEntity sched : flightSchedule) {           
           seatsInventorySessionBean.deleteSeatInventory(sched.getSeatInventory()); 
           em.remove(sched);
       }
    }

    @Override
    public List<FlightScheduleEntity> getFlightSchedules(String departure, String destination, Date date, CabinClassTypeEnum cabin) throws FlightNotFoundException {
        List<FlightScheduleEntity> schedule = new ArrayList<>();
        List<FlightEntity> flight = flightSessionBean.retrieveAllFlightByFlightRoute(departure, destination);

        for (FlightEntity flightEntity: flight) {
            for (FlightSchedulePlanEntity flightSchedulePlanEntity: flightEntity.getFlightSchedulePlan()) {
                if (flightSchedulePlanEntity.isDisabled()) {
                    continue;
                }
                for (FlightScheduleEntity flightScheduleEntity: flightSchedulePlanEntity.getFlightSchedule()) {
                    boolean toAdd = false;
                    if (cabin == null) {
                        toAdd = true;
                    } else {
                        for (SeatInventoryEntity seatInventoryEntity: flightScheduleEntity.getSeatInventory()) {
                            if (seatInventoryEntity.getCabin().getCabinClassType().equals(cabin)) {
                                toAdd = true;
                            }
                        }
                    }
                    
                    Calendar c1 = Calendar.getInstance();
                    Calendar c2 = Calendar.getInstance();
                    c1.setTime(flightScheduleEntity.getDepartureDateTime());
                    c2.setTime(date);
                    boolean sameDay = c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) &&
                                      c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);       
                    if (!sameDay){
                        toAdd = false;
                    }
                    if (toAdd) {
                        schedule.add(flightScheduleEntity);
                    }
                }
            } 
        }       
        Collections.sort(schedule, new FlightScheduleEntity.FlightScheduleComparator());
        return schedule;
    }
    
    @Override
    public List<FlightScheduleEntity> getFlightSchedulesUnmanaged(String departure, String destination, Date date, CabinClassTypeEnum cabin) throws FlightNotFoundException {
        List<FlightScheduleEntity> schedule = new ArrayList<>();
        List<FlightEntity> flight = flightSessionBean.retrieveAllFlightByFlightRoute(departure, destination);

        for (FlightEntity flightEntity: flight) {
            for (FlightSchedulePlanEntity flightSchedulePlanEntity: flightEntity.getFlightSchedulePlan()) {
                if (flightSchedulePlanEntity.isDisabled()) {
                    continue;
                }
                for (FlightScheduleEntity flightScheduleEntity: flightSchedulePlanEntity.getFlightSchedule()) {
                    boolean toAdd = false;
                    if (cabin == null) {
                        toAdd = true;
                    } else {
                        for (SeatInventoryEntity seatInventoryEntity: flightScheduleEntity.getSeatInventory()) {
                            if (seatInventoryEntity.getCabin().getCabinClassType().equals(cabin)) {
                                toAdd = true;
                            }
                        }
                    }
                    
                    Calendar c1 = Calendar.getInstance();
                    Calendar c2 = Calendar.getInstance();
                    c1.setTime(flightScheduleEntity.getDepartureDateTime());
                    c2.setTime(date);
                    boolean sameDay = c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) &&
                                      c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);       
                    if (!sameDay){
                        toAdd = false;
                    }
                    if (toAdd) {
                        em.detach(flightScheduleEntity);
                        schedule.add(flightScheduleEntity);
                    }
                }
            } 
        }       
        Collections.sort(schedule, new FlightScheduleEntity.FlightScheduleComparator());
        return schedule;
    }

    @Override
    public List<Pair<FlightScheduleEntity, FlightScheduleEntity>> getIndirectFlightSchedules(String departure, String destination, Date date, CabinClassTypeEnum cabin) throws FlightNotFoundException {
        List<Pair<FlightScheduleEntity, FlightScheduleEntity>> schedule = new ArrayList<>();
        List<FlightEntity[]> flight = flightSessionBean.retrieveAllIndirectFlightByFlightRoute(departure, destination);
        
        for (Object[] pair: flight) {
            FlightEntity firstFlight = (FlightEntity) pair[0];
            FlightEntity secondFlight = (FlightEntity) pair[1];
            for (FlightSchedulePlanEntity flightSchedulePlan: firstFlight.getFlightSchedulePlan()) {
                if (flightSchedulePlan.isDisabled()) {
                    continue;
                }
                for (FlightScheduleEntity flightSchedule: flightSchedulePlan.getFlightSchedule()) {
                    for (FlightSchedulePlanEntity flightSchedulePlan2: secondFlight.getFlightSchedulePlan()) {
                        if (flightSchedulePlan2.isDisabled()) {
                            continue;
                        }
                        for (FlightScheduleEntity flightSchedule2: flightSchedulePlan2.getFlightSchedule()) {
                            boolean toAdd = false;
                            if (cabin == null) {
                                toAdd = true;
                            } else {
                                for (SeatInventoryEntity seatInventory: flightSchedule.getSeatInventory()) {
                                    for (SeatInventoryEntity seatInventory2: flightSchedule2.getSeatInventory()) {
                                        if (seatInventory.getCabin().getCabinClassType().equals(cabin) && seatInventory2.getCabin().getCabinClassType().equals(cabin)) {
                                        toAdd = true;
                                        }
                                    }                           
                                }
                            }
                            
                            Calendar ca = Calendar.getInstance();
                            Calendar cc = Calendar.getInstance();
                            ca.setTime(flightSchedule.getDepartureDateTime());
                            cc.setTime(date);
                            boolean sameDay = ca.get(Calendar.DAY_OF_YEAR) == cc.get(Calendar.DAY_OF_YEAR) &&
                                              ca.get(Calendar.YEAR) == cc.get(Calendar.YEAR);       
                            if (!sameDay){
                                toAdd = false;
                            }
                            
                            Calendar c = Calendar.getInstance();
                            c.setTime(flightSchedule.getDepartureDateTime());
                            double duration = flightSchedule.getDuration();
                            int hour = (int) duration;
                            int min = (int) (duration % 1 * 60);
                            c.add(Calendar.HOUR_OF_DAY, hour);
                            c.add(Calendar.MINUTE, min);               
                            int diff1 = flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestination().getGmt() - 
                            flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getOrigin().getGmt();
                            c.add(Calendar.HOUR_OF_DAY, diff1);
                            
                            Calendar c2 = Calendar.getInstance();
                            c2.setTime(flightSchedule2.getDepartureDateTime());
                            long gap = Duration.between(c.toInstant(), c2.toInstant()).toHours();
                            if (gap < 2l || gap > 12l) { // To check: how far is too far
                                toAdd = false;
                            }
                            
                            if (toAdd) {
                                schedule.add(new Pair(flightSchedule, flightSchedule2));
                            }
                            
                        }
                    }
                }
            }
        }
        Collections.sort(schedule, new FlightScheduleEntity.IndirectFlightScheduleComparator());
        return schedule;
    }
    
    @Override
    public List<Pair<FlightScheduleEntity, FlightScheduleEntity>> getIndirectFlightSchedulesUnmanaged(String departure, String destination, Date date, CabinClassTypeEnum cabin) throws FlightNotFoundException {
        List<Pair<FlightScheduleEntity, FlightScheduleEntity>> schedule = new ArrayList<>();
        List<FlightEntity[]> flight = flightSessionBean.retrieveAllIndirectFlightByFlightRoute(departure, destination);
        
        for (Object[] pair: flight) {
            FlightEntity firstFlight = (FlightEntity) pair[0];
            FlightEntity secondFlight = (FlightEntity) pair[1];
            for (FlightSchedulePlanEntity flightSchedulePlan: firstFlight.getFlightSchedulePlan()) {
                if (flightSchedulePlan.isDisabled()) {
                    continue;
                }
                for (FlightScheduleEntity flightSchedule: flightSchedulePlan.getFlightSchedule()) {
                    for (FlightSchedulePlanEntity flightSchedulePlan2: secondFlight.getFlightSchedulePlan()) {
                        if (flightSchedulePlan2.isDisabled()) {
                            continue;
                        }
                        for (FlightScheduleEntity flightSchedule2: flightSchedulePlan2.getFlightSchedule()) {
                            boolean toAdd = false;
                            if (cabin == null) {
                                toAdd = true;
                            } else {
                                for (SeatInventoryEntity seatInventory: flightSchedule.getSeatInventory()) {
                                    for (SeatInventoryEntity seatInventory2: flightSchedule2.getSeatInventory()) {
                                        if (seatInventory.getCabin().getCabinClassType().equals(cabin) && seatInventory2.getCabin().getCabinClassType().equals(cabin)) {
                                        toAdd = true;
                                        }
                                    }                           
                                }
                            }
                            
                            Calendar ca = Calendar.getInstance();
                            Calendar cc = Calendar.getInstance();
                            ca.setTime(flightSchedule.getDepartureDateTime());
                            cc.setTime(date);
                            boolean sameDay = ca.get(Calendar.DAY_OF_YEAR) == cc.get(Calendar.DAY_OF_YEAR) &&
                                              ca.get(Calendar.YEAR) == cc.get(Calendar.YEAR);       
                            if (!sameDay){
                                toAdd = false;
                            }
                            
                            Calendar c = Calendar.getInstance();
                            c.setTime(flightSchedule.getDepartureDateTime());
                            double duration = flightSchedule.getDuration();
                            int hour = (int) duration;
                            int min = (int) (duration % 1 * 60);
                            c.add(Calendar.HOUR_OF_DAY, hour);
                            c.add(Calendar.MINUTE, min);               
                            int diff1 = flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestination().getGmt() - 
                            flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getOrigin().getGmt();
                            c.add(Calendar.HOUR_OF_DAY, diff1);
                            
                            Calendar c2 = Calendar.getInstance();
                            c2.setTime(flightSchedule2.getDepartureDateTime());
                            long gap = Duration.between(c.toInstant(), c2.toInstant()).toHours();
                            if (gap < 2l || gap > 12l) { // To check: how far is too far
                                toAdd = false;
                            }
                            
                            if (toAdd) {
                                em.detach(flightSchedule);
                                em.detach(flightSchedule2);
                                schedule.add(new Pair(flightSchedule, flightSchedule2));
                            }
                            
                        }
                    }
                }
            }
        }
        Collections.sort(schedule, new FlightScheduleEntity.IndirectFlightScheduleComparator());
        return schedule;
    }

    @Override
    public FareEntity getSmallestFare(FlightScheduleEntity flightSchedule, CabinClassTypeEnum cabinClassType) throws FlightScheduleNotFoundException, CabinClassNotFoundException {
        FlightScheduleEntity flightScheduleEntity = retrieveFlightScheduleById(flightSchedule.getFlightScheduleID());
        System.out.println("this1: " + flightScheduleEntity.getFlightSchedulePlan().getFares().size());
        List<FareEntity> fares = flightScheduleEntity.getFlightSchedulePlan().getFares();
        System.out.println("this : " + fares.size());
        List<FareEntity> ccfares = new ArrayList<>();
        for (FareEntity fare: fares) {
            if (fare.getCabinClassType().equals(cabinClassType)) {
                ccfares.add(fare);
            }
        }
        if (ccfares.isEmpty()) {
            throw new CabinClassNotFoundException("Cabin class not found");
        }
        FareEntity smallest = ccfares.get(0);
        for (FareEntity fare : ccfares) {
            if(fare.getFareAmount().compareTo(smallest.getFareAmount()) < 0) {
                smallest = fare;
            }
        }
        return smallest;
    }

    @Override
    public FareEntity getBiggestFareUnmanaged(FlightScheduleEntity flightScheduleEntity, CabinClassTypeEnum type) throws FlightScheduleNotFoundException, CabinClassNotFoundException {
        FlightScheduleEntity flightSchedule = retrieveFlightScheduleById(flightScheduleEntity.getFlightScheduleID());
        List<FareEntity> fares = flightSchedule.getFlightSchedulePlan().getFares();
        List<FareEntity> ccfares = new ArrayList<>();
        for(FareEntity fare : fares) {
            if(fare.getCabinClassType().equals(type)) {
                ccfares.add(fare);
            }
        }
        if(ccfares.isEmpty()) {
            throw new CabinClassNotFoundException("Cabin Class not found");
        }
        
        FareEntity biggest = ccfares.get(0);
        for(FareEntity fare : ccfares) {
            if(fare.getFareAmount().compareTo(biggest.getFareAmount()) > 0) {
                biggest = fare;
            }
        }
        em.detach(biggest);
        return biggest;
    }
    
    @Override
    public SeatInventoryEntity getCorrectSeatInventory(FlightScheduleEntity flightSchedule, CabinClassTypeEnum cabinClassType) throws FlightScheduleNotFoundException, SeatInventoryNotFoundException {
        FlightScheduleEntity flightScheduleEntity = retrieveFlightScheduleById(flightSchedule.getFlightScheduleID());
        for (SeatInventoryEntity seat: flightScheduleEntity.getSeatInventory()) {
            if (seat.getCabin().getCabinClassType() == cabinClassType) {
                return seat;
            }
        }
        throw new SeatInventoryNotFoundException("No such seat inventory");
    }
    
    @Override
    public SeatInventoryEntity getCorrectSeatInventoryUnmanaged(FlightScheduleEntity flightSchedule, CabinClassTypeEnum cabinClassType) throws FlightScheduleNotFoundException, SeatInventoryNotFoundException {
        FlightScheduleEntity flightScheduleEntity = retrieveFlightScheduleById(flightSchedule.getFlightScheduleID());
        for (SeatInventoryEntity seat: flightScheduleEntity.getSeatInventory()) {
            if (seat.getCabin().getCabinClassType() == cabinClassType) {
                em.detach(seat);
                return seat;
            }
        }
        throw new SeatInventoryNotFoundException("No such seat inventory");
    }
    
    @Override
    public FlightScheduleEntity updateFlightSchedule(long flightScheduleId, Date newDepartureDateTime, double newFlightDuration) throws FlightScheduleNotFoundException, UpdateFlightScheduleException {
        FlightScheduleEntity flightSchedule = retrieveFlightScheduleById(flightScheduleId);
    
         // Check no overlaps with already existing flight plans associated with the flight
        for (FlightSchedulePlanEntity fsp: flightSchedule.getFlightSchedulePlan().getFlight().getFlightSchedulePlan()) {
            for (FlightScheduleEntity fs: fsp.getFlightSchedule()) {
                if (fs.getFlightScheduleID() == flightScheduleId) {
                    continue;
                }
                Date start1 = fs.getDepartureDateTime();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(start1);
                double duration = fs.getDuration();
                int hour = (int) duration;
                int min = (int) (duration % 1 * 60);
                calendar.add(Calendar.HOUR_OF_DAY, hour);
                calendar.add(Calendar.MINUTE, min);
                Date end1 = calendar.getTime();
            
                Calendar c = Calendar.getInstance();
                c.setTime(newDepartureDateTime);
                double duration2 = newFlightDuration;
                int hour2 = (int) duration2;
                int min2 = (int) (duration % 1 * 60);
                c.add(Calendar.HOUR_OF_DAY, hour2);
                c.add(Calendar.MINUTE, min2);
                Date end2 = c.getTime();
                    
                if (start1.before(end2) && newDepartureDateTime.before(end1)) {
                    //System.out.println("calling one");
                    throw new UpdateFlightScheduleException("Updated fight schedule conflicts with existing flight schedules");
                }   
            }
        }
        
        flightSchedule.setDepartureDateTime(newDepartureDateTime);
        flightSchedule.setDuration(newFlightDuration);
        em.flush();
        return flightSchedule;
    }
    
    @Override
    public void deleteFlightSchedule(long flightScheduleId)  throws FlightScheduleNotFoundException, UpdateFlightScheduleException  {
        FlightScheduleEntity flightSchedule = retrieveFlightScheduleById(flightScheduleId);
        if (!flightSchedule.getReservations().isEmpty()) {
            throw new UpdateFlightScheduleException("Ticket has already been issued for this flight schedule, unable to delete");
        } else {
            flightSchedule.getFlightSchedulePlan().getFlightSchedule().remove(flightSchedule);
            for (SeatInventoryEntity seats: flightSchedule.getSeatInventory()) {    
                em.remove(seats);
            }
            em.remove(flightSchedule);
        }
    }
        
   
}
    
    


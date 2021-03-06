/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FareEntity;
import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import entity.SeatInventoryEntity;
import enumeration.CabinClassTypeEnum;
import exceptions.CabinClassNotFoundException;
import exceptions.FlightNotFoundException;
import exceptions.FlightScheduleNotFoundException;
import exceptions.InputDataValidationException;
import exceptions.SeatInventoryNotFoundException;
import java.util.Date;
import java.util.List;
import javafx.util.Pair;
import javax.ejb.Local;

/**
 *
 * @author Ooi Jun Hao
 */
@Local
public interface FlightScheduleSessionBeanLocal {

    public FlightScheduleEntity createNewSchedule(FlightSchedulePlanEntity flightSchedulePlan, FlightScheduleEntity schedule) throws InputDataValidationException;
    
    public FlightScheduleEntity retrieveFlightScheduleById(Long flightScheduleID) throws FlightScheduleNotFoundException;

    public void deleteSchedule(List<FlightScheduleEntity> flightSchedule);

    public List<FlightScheduleEntity> getFlightSchedules(String departure, String destination, Date date, CabinClassTypeEnum cabin) throws FlightNotFoundException;

    //public FareEntity getBiggestFare(FlightScheduleEntity flightScheduleEntity, CabinClassTypeEnum type) throws FlightScheduleNotFoundException, CabinClassNotFoundException;

    public List<Pair<FlightScheduleEntity, FlightScheduleEntity>> getIndirectFlightSchedules(String departure, String destination, Date date, CabinClassTypeEnum cabin)  throws FlightNotFoundException;

    public SeatInventoryEntity getCorrectSeatInventory(FlightScheduleEntity flightSchedule, CabinClassTypeEnum cabinClassType) throws FlightScheduleNotFoundException, SeatInventoryNotFoundException;

    public SeatInventoryEntity getCorrectSeatInventoryUnmanaged(FlightScheduleEntity flightSchedule, CabinClassTypeEnum cabinClassType) throws FlightScheduleNotFoundException, SeatInventoryNotFoundException;

    public List<FlightScheduleEntity> getFlightSchedulesUnmanaged(String departure, String destination, Date date, CabinClassTypeEnum cabin) throws FlightNotFoundException;

    public List<Pair<FlightScheduleEntity, FlightScheduleEntity>> getIndirectFlightSchedulesUnmanaged(String departure, String destination, Date date, CabinClassTypeEnum cabin) throws FlightNotFoundException;

    public FlightScheduleEntity retrieveFlightScheduleByIdUnmanaged(Long flightScheduleID) throws FlightScheduleNotFoundException;

    public FareEntity getBiggestFareUnmanaged(FlightScheduleEntity flightScheduleEntity, CabinClassTypeEnum type) throws FlightScheduleNotFoundException, CabinClassNotFoundException;
    
}

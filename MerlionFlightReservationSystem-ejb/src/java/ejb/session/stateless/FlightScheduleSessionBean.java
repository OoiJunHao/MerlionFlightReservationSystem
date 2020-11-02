/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package ejb.session.stateless;

import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import exceptions.FlightScheduleNotFoundException;
import exceptions.InputDataValidationException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private FlightSchedulePlanSessionBeanLocal flightSchedulePlanSessionBean;

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
    public FlightScheduleEntity retrieveEarliestDepartureSchedule(List<FlightScheduleEntity> list) throws FlightScheduleNotFoundException {
        FlightScheduleEntity result = null;
        for(int i = 0; i<list.size(); i++) {
            try {
                FlightScheduleEntity test = retrieveFlightScheduleById(list.get(i).getFlightScheduleID());
                if(result == null || result.getDepartureDateTime().compareTo(test.getDepartureDateTime()) > 0) {
                    result = test;
                }
            } catch (FlightScheduleNotFoundException ex) {
                throw new FlightScheduleNotFoundException(ex.getMessage());
            }
        }
        return result;
    }

    @Override
    public void deleteSchedule(List<FlightScheduleEntity> flightSchedule) throws FlightScheduleNotFoundException {
       for(FlightScheduleEntity sched : flightSchedule) {
           sched = retrieveFlightScheduleById(sched.getFlightScheduleID());
           em.remove(sched);
       }
    }

   
}
    
    


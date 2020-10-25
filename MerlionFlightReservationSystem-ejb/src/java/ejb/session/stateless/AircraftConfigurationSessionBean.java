/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.AircraftTypeEntity;
import entity.CabinClassEntity;
import exceptions.AircraftConfigExistException;
import exceptions.AircraftConfigNotFoundException;
import exceptions.AircraftTypeNotFoundException;
import exceptions.UnknownPersistenceException;
import exceptions.CreateNewAircraftConfigException;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

/**
 * @author Ong Bik Jeun
 * @author Ooi Jun Hao
 */
@Stateless
public class AircraftConfigurationSessionBean implements AircraftConfigurationSessionBeanRemote, AircraftConfigurationSessionBeanLocal {

    @Resource
    private EJBContext eJBContext;
    
    @EJB
    private CabinClassSessionBeanLocal cabinClassSessionBean;

    @EJB
    private AircraftTypeSessionBeanLocal aircraftTypeSessionBean;

    @PersistenceContext(unitName = "MerlionFlightReservationSystem-ejbPU")
    private EntityManager em;

    public AircraftConfigurationSessionBean() {
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public AircraftConfigurationEntity createNewAircraftConfig(AircraftConfigurationEntity aircraftConfig, List<CabinClassEntity> cabinClasses) throws CreateNewAircraftConfigException, AircraftConfigExistException, UnknownPersistenceException {
        try {       //TODO: need to associate with aircraft type and the opposite association (more importantly the opposite direction)
            em.persist(aircraftConfig);
            //em.flush(); //QN: cannot flush here else the rollback wont work?             
            for (CabinClassEntity cce: cabinClasses) {
                cabinClassSessionBean.createNewCabinClass(cce, aircraftConfig);
            }
            int maxCapacity = calculateMaxCapacity(aircraftConfig);
            if(aircraftConfig.getAircraftType().getMaxCapacity() >= maxCapacity) {                
                em.flush(); //QN: What if i dont flush at all and wait for exit? any way to catch persistenceExcep?
                return aircraftConfig;
            } else {
                throw new CreateNewAircraftConfigException("Configuration exceeds maximum capacity of aircraft type");
            } 
        } catch (CreateNewAircraftConfigException ex) { //exception thrown due to exceeding max capacity
            eJBContext.setRollbackOnly();
            throw new CreateNewAircraftConfigException(ex.getMessage());
        } catch (PersistenceException ex) { //excpetion thrown from persisting aircraftConfig
            eJBContext.setRollbackOnly();
            if (ex.getCause() != null && ex.getCause().getClass().getName().equals("org.eclipse.persistence.exceptions.DatabaseException")) {
                if (ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getName().equals("java.sql.SQLIntegrityConstraintViolationException")) {
                    throw new AircraftConfigExistException("Configuration name already exists");
                } else {
                    throw new UnknownPersistenceException(ex.getMessage());
                }
            } else {
                throw new UnknownPersistenceException(ex.getMessage());
            }
        }
    }
    
    @Override
    public AircraftConfigurationEntity retriveAircraftConfigByID(Long aircraftConfigID) throws AircraftConfigNotFoundException {
        AircraftConfigurationEntity config = em.find(AircraftConfigurationEntity.class, aircraftConfigID);
        if(config != null) {
            return config;
        } else {
            throw new AircraftConfigNotFoundException("Aircraft Configuration with " + aircraftConfigID + " does not exist!\n");
        }
    }
    
    @Override
    public int calculateMaxCapacity(AircraftConfigurationEntity aircraftConfig) {
        int max = 0;
        System.out.println("Number of cabins: " + aircraftConfig.getCabin().size());
        for (CabinClassEntity cabin: aircraftConfig.getCabin()) {
            max += cabin.getMaxSeatCapacity();
        }
        System.out.println("/nTesting max: " + max);
        return max;
    }

    @Override
    public void associateTypeWithConfig(Long i, Long aircraftConfigID) {
        try {
            AircraftTypeEntity type = aircraftTypeSessionBean.retrieveAircraftTypeById(i);
            AircraftConfigurationEntity config = retriveAircraftConfigByID(aircraftConfigID);
            
            if(type != null && config != null) {
                type.getAircraftConfig().add(config);
                config.setAircraftType(type);
            }
        } catch (AircraftTypeNotFoundException | AircraftConfigNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public List<AircraftConfigurationEntity> retrieveAllConfiguration() {
        Query query = em.createQuery("SELECT a FROM AircraftConfigurationEntity a ORDER BY a.aircraftType ASC, a.name ASC ");
        return query.getResultList();
    } 
}

    
    
    

   
    

   


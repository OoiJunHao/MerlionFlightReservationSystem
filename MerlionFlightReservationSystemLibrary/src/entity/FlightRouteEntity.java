/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.ArrayList;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author Ong Bik Jeun
 */
@Entity
@Table(uniqueConstraints=
       @UniqueConstraint(columnNames = {"origin", "destination"})) 
public class FlightRouteEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightRouteID;
    
    @OneToMany(mappedBy = "flightRoute", fetch = FetchType.EAGER)
    private ArrayList<FlightEntity> flights;
    
    @ManyToOne
    @JoinColumn(name = "origin")
    private AirportEntity origin;
    
    @ManyToOne
    @JoinColumn(name = "destination")
    private AirportEntity destination;
    
    //One-to-One self referencing constraints (please check)
    //QN: Why is a self referencing like this?? 
    @OneToOne
    private FlightRouteEntity complementaryRoute;
    @OneToOne(mappedBy = "complementaryRoute")
    private FlightRouteEntity sourceRoute;
   
    public FlightRouteEntity() {
        flights = new ArrayList<>();  
    }

    public FlightRouteEntity(AirportEntity origin, AirportEntity destination) {
        this();
        this.origin = origin;
        this.destination = destination;
    }
    
    public Long getFlightRouteID() {
        return flightRouteID;
    }

    public void setFlightRouteID(Long flightRouteID) {
        this.flightRouteID = flightRouteID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (flightRouteID != null ? flightRouteID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the flightRouteID fields are not set
        if (!(object instanceof FlightRouteEntity)) {
            return false;
        }
        FlightRouteEntity other = (FlightRouteEntity) object;
        if ((this.flightRouteID == null && other.flightRouteID != null) || (this.flightRouteID != null && !this.flightRouteID.equals(other.flightRouteID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.FlightRouteEntity[ id=" + flightRouteID + " ]";
    }

    /**
     * @return the flights
     */
    public ArrayList<FlightEntity> getFlights() {
        return flights;
    }

    /**
     * @param flights the flights to set
     */
    public void setFlights(ArrayList<FlightEntity> flights) {
        this.flights = flights;
    }

    /**
     * @return the origin
     */
    public AirportEntity getOrigin() {
        return origin;
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(AirportEntity origin) {
        this.origin = origin;
    }

    /**
     * @return the destination
     */
    public AirportEntity getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(AirportEntity destination) {
        this.destination = destination;
    }

    /**
     * @return the complementaryRoute
     */
    public FlightRouteEntity getComplementaryRoute() {
        return complementaryRoute;
    }

    /**
     * @param complementaryRoute the complementaryRoute to set
     */
    public void setComplementaryRoute(FlightRouteEntity complementaryRoute) {
        this.complementaryRoute = complementaryRoute;
    }

    /**
     * @return the sourceRoute
     */
    public FlightRouteEntity getSourceRoute() {
        return sourceRoute;
    }

    /**
     * @param sourceRoute the sourceRoute to set
     */
    public void setSourceRoute(FlightRouteEntity sourceRoute) {
        this.sourceRoute = sourceRoute;
    }
    
}

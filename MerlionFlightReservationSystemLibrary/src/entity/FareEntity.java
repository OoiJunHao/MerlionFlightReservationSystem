/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package entity;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author Mitsuki
 */
@Entity
public class FareEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fareID;
    /* Fare codes start with a
    letter that denotes the booking class. Other letters or numbers may follow. Typically a fare
    basis will be 3 to 7 characters long.*/
    @Column(nullable = false, unique = true, length = 7)
    private String fareBasisCode;
    @Column(nullable = false, precision = 11, scale = 2)
    private BigDecimal fareAmount;
    
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private CabinClassEntity cabin;

    public FareEntity() {
    }

    public FareEntity(String fareBasisCode, BigDecimal fareAmount, CabinClassEntity cabin) {
        this.fareBasisCode = fareBasisCode;
        this.fareAmount = fareAmount;
        this.cabin = cabin;
    }

    public String getFareBasisCode() {
        return fareBasisCode;
    }

    public void setFareBasisCode(String fareBasisCode) {
        this.fareBasisCode = fareBasisCode;
    }

    public BigDecimal getFareAmount() {
        return fareAmount;
    }

    public void setFareAmount(BigDecimal fareAmount) {
        this.fareAmount = fareAmount;
    }

    public CabinClassEntity getCabin() {
        return cabin;
    }

    public void setCabin(CabinClassEntity cabin) {
        this.cabin = cabin;
    }
    
    
    
    
    
    
    public Long getFareID() {
        return fareID;
    }
    
    public void setFareID(Long fareID) {
        this.fareID = fareID;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (fareID != null ? fareID.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the fareID fields are not set
        if (!(object instanceof FareEntity)) {
            return false;
        }
        FareEntity other = (FareEntity) object;
        if ((this.fareID == null && other.fareID != null) || (this.fareID != null && !this.fareID.equals(other.fareID))) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "entity.FareEntity[ id=" + fareID + " ]";
    }
    
}
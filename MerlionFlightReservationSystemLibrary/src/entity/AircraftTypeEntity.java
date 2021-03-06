/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Ong Bik Jeun
 */
@Entity
public class AircraftTypeEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aircraftID;
    
    @Column(nullable = false, length = 32)
    @Size(min = 1, max = 32)
    @NotNull
    private String typeName;
    
    @Column(nullable = false)
    @Min(0)
    @NotNull
    private int maxCapacity;
    
    @OneToMany(mappedBy = "aircraftType", fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    private List<AircraftConfigurationEntity> aircraftConfig;

    public AircraftTypeEntity() {
        aircraftConfig = new ArrayList<>();
    }

    public AircraftTypeEntity(String typeName, int maxCapacity) {
        this();
        this.typeName = typeName;
        this.maxCapacity = maxCapacity;
    }

    public AircraftTypeEntity(String typeName, int maxCapacity, List<AircraftConfigurationEntity> aircraftConfig) {
        this();
        this.typeName = typeName;
        this.maxCapacity = maxCapacity;
        this.aircraftConfig = aircraftConfig;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public List<AircraftConfigurationEntity> getAircraftConfig() {
        return aircraftConfig;
    }

    public void setAircraftConfig(List<AircraftConfigurationEntity> aircraftConfig) {
        this.aircraftConfig = aircraftConfig;
    }

    public Long getAircraftID() {
        return aircraftID;
    }

    public void setAircraftID(Long aircraftID) {
        this.aircraftID = aircraftID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (aircraftID != null ? aircraftID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the aircraftID fields are not set
        if (!(object instanceof AircraftTypeEntity)) {
            return false;
        }
        AircraftTypeEntity other = (AircraftTypeEntity) object;
        if ((this.aircraftID == null && other.aircraftID != null) || (this.aircraftID != null && !this.aircraftID.equals(other.aircraftID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.AircraftType[ id=" + aircraftID + " ]";
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FareEntity;
import exceptions.FareNotFoundException;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author Ooi Jun Hao
 */
@Local
public interface FareSessionBeanLocal {

    public void deleteFares(List<FareEntity> fares) throws FareNotFoundException;
    
}

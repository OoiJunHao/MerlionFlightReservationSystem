/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.PartnerEntity;
import exceptions.InvalidLoginCredentialException;
import exceptions.PartnerUsernameExistException;
import exceptions.UnknownPersistenceException;
import javax.ejb.Local;

/**
 *
 * @author Ooi Jun Hao
 */
@Local
public interface PartnerSessionBeanLocal {

    public PartnerEntity createNewPartner(PartnerEntity partner) throws PartnerUsernameExistException, UnknownPersistenceException;

    public long doLogin(String username, String password) throws InvalidLoginCredentialException;
    
}

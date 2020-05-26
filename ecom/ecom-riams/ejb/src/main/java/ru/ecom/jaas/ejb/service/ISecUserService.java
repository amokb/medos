package ru.ecom.jaas.ejb.service;

import ru.ecom.jaas.ejb.form.SecRoleForm;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import java.io.IOException;
import java.util.Collection;

/**
 * 
 */
public interface ISecUserService {


	String changePassword (String aNewPassword, String aOldPassword, String aUsername)throws IOException;
	void setDefaultPassword (String aNewPassword, String aUsername, String aUsernameChange)throws IOException;
    void fhushJboss() throws ReflectionException, InstanceNotFoundException, MBeanException, MalformedObjectNameException;
    void exportUsersProperties(String aFilename) throws IOException ;
    void exportUsersProperties() throws IOException ;

    void exportRolesProperties(String aFilename) throws IOException ;
    void exportRolesProperties() throws IOException ;

    Collection<SecRoleForm> listUserRoles(long aUserId, boolean aIsSystemView) ;
    void removeRoles(long aUserId, long[] aRoles) ;
    void addRoles(long aUserId, long[] aRoles) ;
    Collection<SecRoleForm> listRolesToAdd(long aUserId, boolean aIsSystemView) ;
    //Добавить пользователя в госпиталь
    String addUserToHosp(Long aUserId, Long aLpuId, Long avWfId, String newPsw, Long userCopy, Long aPatientId)  throws IOException;
    //Добавить пользователя в госпиталь через персонуa
    String addUserToHospFromPerson(Long aPatientId, Long aLpuId, Long avWfId, String newPsw, Long userCopy, String username)  throws IOException;
}

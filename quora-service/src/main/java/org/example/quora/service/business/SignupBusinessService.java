package org.example.quora.service.business;
/***Author : Aaditya Raj
 * Date: 12-02-2021
 * SignupBusiness service for user signup
 ****/
;
import org.example.quora.service.dao.UserDao;
import org.example.quora.service.entity.UserEntity;
import org.example.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupBusinessService {

    //Respective Data access object and passwordcryptogaphyprovider has been autowired to access the method defined in respective Dao
    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;
    
    //Logger logger = LoggerFactory.getLogger(SignupBusinessService.class);


    /**
     * This method checks if the username and email exist in the DB. if the username or email doesn't
     * exist in the DB. Assign encrypted password and salt to the user.
     *
     * @throws SignUpRestrictedException SGR-001 if the username exist in the DB , SGR-002 if the
     *     email exist in the DB.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signup(UserEntity userEntity, String userName, String email) throws SignUpRestrictedException {
        if (userDao.getUserByUserName(userName) != null) {
            throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
        } else if (userDao.getUserByEmail(email) != null) {
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
        } else {
            //Generates encrypted password and salt based on the password provided by user during signup

        	String[] encryptedText = passwordCryptographyProvider.encrypt(userEntity.getPassword());
            userEntity.setSalt(encryptedText[0]);
            userEntity.setPassword(encryptedText[1]);
            return userDao.createUser(userEntity);
        }
    }
}

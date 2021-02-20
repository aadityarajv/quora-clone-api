package org.example.quora.api.controller;

import org.example.quora.api.model.SigninResponse;
import org.example.quora.api.model.SignoutResponse;
import org.example.quora.api.model.SignupUserRequest;
import org.example.quora.api.model.SignupUserResponse;
import org.example.quora.service.business.SigninBusinessService;
import org.example.quora.service.business.SignoutBusinessService;
import org.example.quora.service.business.SignupBusinessService;
import org.example.quora.service.entity.UserAuthTokenEntity;
import org.example.quora.service.entity.UserEntity;
import org.example.quora.service.exception.AuthenticationFailedException;
import org.example.quora.service.exception.SignOutRestrictedException;
import org.example.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.UUID;

/***Author : Aaditya Raj
 * Date: 12-02-2021
 * User Controller class for signup,signin and signout API
 ****/

@RestController
@RequestMapping("/")
public class UserController {

    // Autowiring Requires services to access methods
    @Autowired
    private SignupBusinessService signupBusinessService;

    @Autowired
    private SigninBusinessService signinBusinessService;

    @Autowired
    private SignoutBusinessService signoutBusinessService;

    /**
     * This method is for user signup. This method receives the object of SignupUserRequest type with
     * its attributes being set.
     *
     * @return SignupUserResponse - UUID of the user created.
     * @throws SignUpRestrictedException - if the username or email already exist in the database.
     */
    @RequestMapping(method = RequestMethod.POST, path = "/user/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> signup(final SignupUserRequest signupUserRequest) throws SignUpRestrictedException {
        final UserEntity userEntity = new UserEntity();
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setFirstName(signupUserRequest.getFirstName());
        userEntity.setLastName(signupUserRequest.getLastName());
        userEntity.setEmail(signupUserRequest.getEmailAddress());
        userEntity.setContactNumber(signupUserRequest.getContactNumber());
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setDob(signupUserRequest.getDob());
        userEntity.setUserName(signupUserRequest.getUserName());
        userEntity.setAboutMe(signupUserRequest.getAboutMe());
        userEntity.setSalt("1234abc");
        userEntity.setPassword(signupUserRequest.getPassword());
        userEntity.setRole("admin");
        //call signupservice method signup
        final UserEntity createdUserEntity = signupBusinessService.signup(userEntity,signupUserRequest.getUserName(),signupUserRequest.getEmailAddress());
        SignupUserResponse userResponse = new SignupUserResponse()
                .id(createdUserEntity.getUuid())
                .status("USER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupUserResponse>(userResponse, HttpStatus.CREATED);
    }

    /**
     * This method is for a user to singin.
     *
     * @param: authorization is basic auth (base 64 encoded). Usage: Basic <Base 64 Encoded
     *     username:password>
     * @return SigninResponse which contains user id and a access-token in the response header.
     * @throws AuthenticationFailedException ATH-001 if username doesn't exist, ATH-002 if password is
     *     wrong.
     */
    @RequestMapping(method = RequestMethod.POST, path = "/user/signin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> login(@RequestHeader("authentication") final String authentication) throws AuthenticationFailedException {
        byte[] decode = Base64.getDecoder().decode(authentication.split("Basic ")[1]);
        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");
        //call signinservice method authenticate
        final UserAuthTokenEntity userAuthToken = signinBusinessService.authenticate(decodedArray[0], decodedArray[1]);
        UserEntity user = userAuthToken.getUser();

        SigninResponse signinResponse = new SigninResponse()
                .id(user.getUuid())
                .message("SIGNED IN SUCCESSFULLY");
        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", userAuthToken.getAccessToken());
        return new ResponseEntity<SigninResponse>(  signinResponse, headers, HttpStatus.OK);
    }

    /**
     * This method is used to signout user.
     *
     * @param accessToken Token used for authenticating the user.
     * @return UUID of the user who is signed out.
     * @throws SignOutRestrictedException if the user is not signed in
     */
    @RequestMapping(method=RequestMethod.POST,path="/user/signout",produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> logout(@RequestHeader("authorization") final String accessToken)throws SignOutRestrictedException {
        //call signoutservice method verifyAuthToken
        final UserAuthTokenEntity userAuthTokenEntity=signoutBusinessService.verifyAuthToken(accessToken);
        SignoutResponse signoutResponse=new SignoutResponse()
                .id(userAuthTokenEntity.getUuid())
                .message("SIGNED OUT SUCCESSFULLY");
        return new ResponseEntity<SignoutResponse>(signoutResponse,HttpStatus.OK);
    }

}

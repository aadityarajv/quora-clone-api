package org.example.quora.service.business;

import org.example.quora.service.dao.AnswerDao;
import org.example.quora.service.dao.QuestionDao;
import org.example.quora.service.dao.UserDao;
import org.example.quora.service.entity.AnswerEntity;
import org.example.quora.service.entity.QuestionEntity;
import org.example.quora.service.entity.UserAuthTokenEntity;
import org.example.quora.service.exception.AnswerNotFoundException;
import org.example.quora.service.exception.AuthorizationFailedException;
import org.example.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AnswerService {

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private QuestionDao questionDao;

    /**
     * creates an answer in the database.
     *
     * @param answerEntity Contains the answer content.
     * @param accessToken To authenticate the user who is trying to create an answer.
     * @param questionId Id of the question for which the answer is being created.
     * @return
     * @throws AuthorizationFailedException ATHR-001 If the user has not signed in and ATHR-002 If the
     *     * user is already signed out
     * @throws InvalidQuestionException QUES-001 if the question doesn't exist in database.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(
            AnswerEntity answerEntity, final String accessToken, final String questionId)
            throws AuthorizationFailedException, InvalidQuestionException {

        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(accessToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException(
                    "ATHR-002", "User is signed out.Sign in first to post an answer");
        }
        QuestionEntity questionEntity = questionDao.getQuestionByQUuid(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setDate(ZonedDateTime.now());
        answerEntity.setQuestionEntity(questionEntity);
        answerEntity.setUser(userAuthTokenEntity.getUser());

        return answerDao.createAnswer(answerEntity);
    }

    /**
     * edits the answer which already exist in the database.
     *
     * @param accessToken To authenticate the user who is trying to edit the answer.
     * @param answerId Id of the answe which is to be edited.
     * @param newAnswer Contains the new content of the answer.
     * @return
     * @throws AnswerNotFoundException ANS-001 if the answer is not found in the database.
     * @throws AuthorizationFailedException ATHR-001 If the user has not signed in and ATHR-002 If the
     *     * user is already signed out and ATHR-003 if the user is not the owner of the answer.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswer(
            final String accessToken,final String answerId, final String newAnswer)
            throws AnswerNotFoundException, AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(accessToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException(
                    "ATHR-002", "User is signed out.Sign in first to edit an answer");
        }
        AnswerEntity answerEntity = answerDao.getAnswerById(answerId);
        if (answerEntity == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }
        if (!answerEntity.getUser().getUuid().equals(userAuthTokenEntity.getUser().getUuid())) {
            throw new AuthorizationFailedException(
                    "ATHR-003", "Only the answer owner can edit the answer");
        }
        answerEntity.setAnswer(newAnswer);
        answerDao.updateAnswer(answerEntity);
        return answerEntity;
    }

    /**
     * delete the answer
     *
     * @param answerId id of the answer to be deleted.
     * @param accessToken accessToken of the user for valid authentication.
     * @throws AuthorizationFailedException ATHR-001 - if User has not signed in. ATHR-002 if the User
     *     is signed out. ATHR-003 if non admin or non owner of the answer tries to delete the answer.
     * @throws AnswerNotFoundException if the answer with id doesn't exist.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(
            final String answerId, final String accessToken)
            throws  AnswerNotFoundException, AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(accessToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException(
                    "ATHR-002", "User is signed out.Sign in first to delete an answer");
        }
        AnswerEntity answerEntity = answerDao.getAnswerById(answerId);
        if (answerEntity == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }
        if (userAuthTokenEntity.getUser().getRole().equals("admin")
                || answerEntity
                .getUser()
                .getUuid()
                .equals(userAuthTokenEntity.getUser().getUuid())) {
            return answerDao.deleteAnswer(answerId);
        } else {
            throw new AuthorizationFailedException(
                    "ATHR-003", "Only the answer owner or admin can delete the answer");
        }
    }

    /**
     * get all the answers for a question
     *
     * @param questionId id of the question to fetch the answers.
     * @param accessToken accessToken of the user for valid authentication.
     * @throws AuthorizationFailedException ATHR-001 - if User has not signed in. ATHR-002 if the User
     *     is signed out.
     * @throws InvalidQuestionException The question with entered uuid whose details are to be seen
     *     does not exist.
     */
    public List<AnswerEntity> getAllAnswersToQuestion(
            final String questionId, final String accessToken) throws  InvalidQuestionException,AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(accessToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException(
                    "ATHR-002", "User is signed out.Sign in first to get the answers");
        }
        QuestionEntity questionEntity = questionDao.getQuestionByQUuid(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException(
                    "QUES-001", "The question with entered uuid whose details are to be seen does not exist");
        }
        return answerDao.getAllAnswersToQuestion(questionId);
    }

    public String getQuestionContentByQuestionId(String questionId) {
        QuestionEntity questionEntity = questionDao.getQuestionByQUuid(questionId);
        return questionEntity.getContent();
    }
}
